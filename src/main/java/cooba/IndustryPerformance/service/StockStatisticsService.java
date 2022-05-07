package cooba.IndustryPerformance.service;

import cooba.IndustryPerformance.database.entity.StockDetail.StockDetail;
import cooba.IndustryPerformance.database.entity.StockStatistics.StockStatistics;
import cooba.IndustryPerformance.database.repository.StockDetailRepository;
import cooba.IndustryPerformance.database.repository.StockStatisticsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class StockStatisticsService {
    @Autowired
    StockDetailRepository stockDetailRepository;
    @Autowired
    StockStatisticsRepository stockStatisticsRepository;

    public void calculateStockStatistics(String stockcode, LocalDate startDate, LocalDate endDate) {
        List<StockDetail> stockDetailList = stockDetailRepository.findByStockcodeAndCreatedTimeBetween(stockcode, startDate, endDate);
        List<BigDecimal> avgCostList = new ArrayList<>();
        List<LocalDate> dateList = new ArrayList<>();
        stockDetailList.forEach(stockDetail -> {
            System.out.println(stockDetail.getCreatedTime());
            BigDecimal avgCost = BigDecimal.valueOf((float) stockDetail.getTurnover() / stockDetail.getSharesTraded()).setScale(2, RoundingMode.HALF_UP);
            avgCostList.add(avgCost);
            dateList.add(stockDetail.getCreatedTime());
        });

        List<BigDecimal> avg5dCostList = countNdAvgCost(avgCostList, 5);
        List<BigDecimal> avg10dCostList = countNdAvgCost(avgCostList, 10);
        List<BigDecimal> avg30dCostList = countNdAvgCost(avgCostList, 30);

        saveData(stockcode, dateList, avgCostList, avg5dCostList, avg10dCostList, avg30dCostList);
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

    public void saveData(String stockcode, List<LocalDate> dateList,
                         List<BigDecimal> avgCostList, List<BigDecimal> avg5dCostList, List<BigDecimal> avg10dCostList, List<BigDecimal> avg30dCostList) {
        for (int i = 0; i < dateList.size(); i++) {
            StockStatistics stockStatistics = new StockStatistics();
            stockStatistics.setStockcode(stockcode);
            stockStatistics.setTradingDate(dateList.get(i));
            stockStatistics.setAvgCost(avgCostList.get(i));
            if (i < avg5dCostList.size()) stockStatistics.setAvg5dCost(avgCostList.get(i));
            if (i < avg10dCostList.size()) stockStatistics.setAvg10dCost(avg10dCostList.get(i));
            if (i < avg30dCostList.size()) stockStatistics.setAvg30dCost(avg30dCostList.get(i));
            stockStatisticsRepository.save(stockStatistics);
        }
    }
}
