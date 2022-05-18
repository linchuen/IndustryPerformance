package cooba.IndustryPerformance.service;

import cooba.IndustryPerformance.database.entity.StockDetail.StockDetail;
import cooba.IndustryPerformance.database.entity.StockStatistics.StockStatistics;
import cooba.IndustryPerformance.database.mapper.StockStatisticsMapper;
import cooba.IndustryPerformance.database.repository.StockDetailRepository;
import cooba.IndustryPerformance.database.repository.StockStatisticsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static cooba.IndustryPerformance.constant.StockConstant.*;

@Slf4j
@Service
public class StockStatisticsService {
    @Autowired
    StockDetailRepository stockDetailRepository;
    @Autowired
    StockStatisticsRepository stockStatisticsRepository;
    @Autowired
    StockStatisticsMapper stockStatisticsMapper;
    @Autowired
    MongoTemplate mongoTemplate;

    @Async("stockExecutor")
    public void calculateStockStatisticsAsync(String stockcode, LocalDate startDate, LocalDate endDate) {
        calculateStockStatistics(stockcode, startDate, endDate);
    }

    public void calculateStockStatistics(String stockcode, LocalDate startDate, LocalDate endDate) {
        List<StockDetail> stockDetailList = stockDetailRepository.findByStockcodeAndCreatedTimeBetweenOrderByCreatedTimeDesc(stockcode, startDate, endDate);
        calculateStockDetailList(stockcode, stockDetailList);
    }

    public void calculateStockStatistics(String stockcode, LocalDate startDate) {
        List<StockDetail> stockDetailList = stockDetailRepository.findByStockcodeAndCreatedTimeBeforeOrderByCreatedTimeDesc(stockcode, startDate);
        calculateStockDetailList(stockcode, stockDetailList);
    }

    public void calculateStockDetailList(String stockcode, List<StockDetail> stockDetailList) {
        List<BigDecimal> avgCostList = new ArrayList<>();
        List<BigDecimal> avgShareList = new ArrayList<>();
        List<LocalDate> dateList = new ArrayList<>();
        stockDetailList.forEach(stockDetail -> {
            BigDecimal avgCost = BigDecimal.valueOf((float) stockDetail.getTurnover() / stockDetail.getSharesTraded()).setScale(2, RoundingMode.HALF_UP);
            avgCostList.add(avgCost);
            BigDecimal avgShare = BigDecimal.valueOf((float) stockDetail.getSharesTraded() / stockDetail.getTradingVolume()).setScale(2, RoundingMode.HALF_UP);
            avgShareList.add(avgShare);
            dateList.add(stockDetail.getCreatedTime());
        });

        List<BigDecimal> avg5dCostList = countNdAvgCost(avgCostList, 5);
        List<BigDecimal> avg10dCostList = countNdAvgCost(avgCostList, 10);
        List<BigDecimal> avg21dCostList = countNdAvgCost(avgCostList, 21);
        List<BigDecimal> avg62dCostList = countNdAvgCost(avgCostList, 62);

        saveListData(stockcode, dateList, avgShareList, avgCostList,
                avg5dCostList, avg10dCostList, avg21dCostList, avg62dCostList);
    }

    public List<BigDecimal> countNdAvgCost(List<BigDecimal> inputAvgCostList, int outputN) {
        if (outputN > inputAvgCostList.size()
                || outputN <= 0
        ) return new ArrayList<>();
        List<BigDecimal> avgNdCostList = new ArrayList<>();

        BigDecimal sum = new BigDecimal(0);
        for (int i = 0; i < outputN; i++) {
            sum = sum.add(inputAvgCostList.get(i));
        }
        avgNdCostList.add(sum.divide(new BigDecimal(outputN), 2, RoundingMode.HALF_UP));
        for (int i = outputN; i < inputAvgCostList.size(); i++) {
            sum = sum.add(inputAvgCostList.get(i)).subtract(inputAvgCostList.get(i - outputN));
            avgNdCostList.add(sum.divide(new BigDecimal(outputN), 2, RoundingMode.HALF_UP));
        }
        return avgNdCostList;
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveListData(String stockcode, List<LocalDate> dateList, List<BigDecimal> avgShareList, List<BigDecimal> avgCostList
            , List<BigDecimal> avg5dCostList, List<BigDecimal> avg10dCostList, List<BigDecimal> avg21dCostList, List<BigDecimal> avg62dCostList) {
        synchronized (LocalcacheService.getStockcodeLock(stockcode)) {
            try {
                for (int i = 0; i < dateList.size(); i++) {
                    StockStatistics stockStatistics = new StockStatistics();
                    stockStatistics.setStockcode(stockcode);
                    stockStatistics.setTradingDate(dateList.get(i));
                    stockStatistics.setAvgShare(avgShareList.get(i));
                    stockStatistics.setAvgCost(avgCostList.get(i));
                    if (i < avg5dCostList.size()) stockStatistics.setAvg5dCost(avg5dCostList.get(i));
                    if (i < avg10dCostList.size()) stockStatistics.setAvg10dCost(avg10dCostList.get(i));
                    if (i < avg21dCostList.size()) stockStatistics.setAvg21dCost(avg21dCostList.get(i));
                    if (i < avg62dCostList.size()) stockStatistics.setAvg62dCost(avg62dCostList.get(i));
                    stockStatisticsRepository.findByStockcodeAndTradingDate(stockcode, dateList.get(i)).ifPresentOrElse(
                            oldStockStatistics -> {
                                stockStatistics.setId(oldStockStatistics.getId());
                                stockStatisticsRepository.save(stockStatistics);
                            },
                            () -> {
                                stockStatisticsRepository.save(stockStatistics);
                            }
                    );

                    stockStatistics.setId(stockStatistics.getTradingDate().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + stockcode);
                    stockStatisticsMapper.insertStockStatistics(stockStatistics);
                }
                log.info("統計數據 股票代碼:{} 寫入db完成", stockcode);
            } catch (Exception e) {
                log.warn("統計數據 股票代碼:{} 寫入db失敗 class:{} error:{}", stockcode, getClass().getName(), e.getMessage());
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean updateStockStatistics(String stockcode, LocalDate date, int n) {
        List<StockDetail> stockDetailList = stockDetailRepository.findTop100ByStockcodeAndCreatedTimeBeforeOrderByCreatedTimeDesc(stockcode, date.plusDays(1));
        BigDecimal result = new BigDecimal(0);
        if (n > stockDetailList.size()) return false;

        stockDetailList.subList(0, n).forEach(stockDetail -> {
            BigDecimal avgCost = BigDecimal.valueOf((float) stockDetail.getTurnover() / stockDetail.getSharesTraded()).setScale(2, RoundingMode.HALF_UP);
            result.add(avgCost.divide(new BigDecimal(n), 2, RoundingMode.HALF_UP));
        });
        Query query = new Query()
                .addCriteria(Criteria.where("createdTime").is(date))
                .addCriteria(Criteria.where("stockcode").is(stockcode));

        Update update = new Update();
        switch (n) {
            case 5:
                update.set(avg5d, result);
            case 10:
                update.set(avg10d, result);
            case 21:
                update.set(avg21d, result);
            case 62:
                update.set(avg62d, result);
        }
        mongoTemplate.findAndModify(query, update, StockStatistics.class, "stockStatistics");
        return true;
    }
}
