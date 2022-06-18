package cooba.IndustryPerformance.service;

import cooba.IndustryPerformance.database.entity.StockDetail.StockDetail;
import cooba.IndustryPerformance.database.entity.StockStatistics.StockStatistics;
import cooba.IndustryPerformance.database.mapper.StockStatisticsMapper;
import cooba.IndustryPerformance.database.repository.StockDetailRepository;
import cooba.IndustryPerformance.database.repository.StockStatisticsRepository;
import cooba.IndustryPerformance.entity.StockDetailStatistics;
import cooba.IndustryPerformance.utility.RedisCacheUtility;
import cooba.IndustryPerformance.utility.RedisUtility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cooba.IndustryPerformance.constant.CommonConstant.YM;
import static cooba.IndustryPerformance.constant.CommonConstant.YMD;
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
    @Autowired
    TimeCounterService timeCounterService;
    @Autowired
    RedisUtility redisUtility;
    @Autowired
    RedisCacheUtility redisCacheUtility;

    @Async("stockExecutor")
    public void calculateStockStatisticsAsync(String uuid, String stockcode, LocalDate startDate, LocalDate endDate) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("calculateStockStatisticsAsync" + stockcode + startDate + endDate);
        calculateStockStatistics(stockcode, startDate, endDate);
        stopWatch.stop();
        System.out.println(stopWatch.prettyPrint());
        Double time = stopWatch.getTotalTimeSeconds();
        timeCounterService.addTime(uuid, time);
    }

    public void calculateStockStatistics(String stockcode, LocalDate startDate, LocalDate endDate) {
        LocalDate start = startDate.minusMonths(3);
        LocalDate end = endDate.plusDays(1);
        List<StockDetail> stockDetailList = stockDetailRepository.findByStockcodeAndCreatedTimeBetweenOrderByCreatedTimeDesc(stockcode, start, end);
        List<StockDetail> filterOut0StockDetailList = stockDetailList.stream()
                .filter(stockDetail -> stockDetail.getPrice().compareTo(BigDecimal.ZERO) != 0)
                .collect(Collectors.toList());
        List<LocalDate> dateList = filterOut0StockDetailList.stream()
                .map(StockDetail::getCreatedTime)
                .filter(localdate -> !localdate.isBefore(startDate) && !localdate.isAfter(endDate))
                .collect(Collectors.toList());
        calculateStockDetailList(stockcode, filterOut0StockDetailList, dateList);
    }

    @Async("stockExecutor")
    public void calculateStockStatisticsStartDateBeforeAsync(String uuid, String stockcode, LocalDate startDate) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("calculateStockStatisticsStartDateBeforeAsync" + stockcode + startDate);
        calculateStockStatisticsStartDateBefore(stockcode, startDate);
        stopWatch.stop();
        System.out.println(stopWatch.prettyPrint());
        Double time = stopWatch.getTotalTimeSeconds();
        timeCounterService.addTime(uuid, time);
    }

    public void calculateStockStatisticsStartDateBefore(String stockcode, LocalDate startDate) {
        List<StockDetail> stockDetailList = stockDetailRepository.findByStockcodeAndCreatedTimeBeforeOrderByCreatedTimeDesc(stockcode, startDate);
        List<StockDetail> filterOut0StockDetailList = stockDetailList.stream()
                .filter(stockDetail -> stockDetail.getPrice().compareTo(BigDecimal.ZERO) != 0)
                .collect(Collectors.toList());
        List<LocalDate> dateList = filterOut0StockDetailList.stream()
                .map(StockDetail::getCreatedTime)
                .collect(Collectors.toList());
        calculateStockDetailList(stockcode, filterOut0StockDetailList, dateList);
    }

    public void calculateStockDetailList(String stockcode, List<StockDetail> stockDetailList, List<LocalDate> dateList) {
        List<BigDecimal> avgCostList = new ArrayList<>();
        List<BigDecimal> avgShareList = new ArrayList<>();
        List<BigDecimal> volumeList = new ArrayList<>();
        if (dateList.isEmpty()) return;

        stockDetailList.forEach(stockDetail -> {
            BigDecimal avgCost = BigDecimal.valueOf((float) stockDetail.getTurnover() / stockDetail.getSharesTraded()).setScale(2, RoundingMode.HALF_UP);
            avgCostList.add(avgCost);
            BigDecimal avgShare = BigDecimal.valueOf((float) stockDetail.getSharesTraded() / stockDetail.getTradingVolume()).setScale(2, RoundingMode.HALF_UP);
            avgShareList.add(avgShare);
            volumeList.add(BigDecimal.valueOf(stockDetail.getTradingVolume()));
        });

        List<BigDecimal> avg5dCostList = countNdAvgList(avgCostList, 5);
        List<BigDecimal> avg10dCostList = countNdAvgList(avgCostList, 10);
        List<BigDecimal> avg21dCostList = countNdAvgList(avgCostList, 21);
        List<BigDecimal> avg62dCostList = countNdAvgList(avgCostList, 62);

        List<BigDecimal> avg10dVolumeList = countNdAvgList(volumeList, 10);
        List<BigDecimal> avg21dVolumeList = countNdAvgList(volumeList, 21);

        try {
            saveListBatchData(stockcode,
                    dateList,
                    avgShareList,
                    avg10dVolumeList,
                    avg21dVolumeList,
                    avgCostList,
                    avg5dCostList,
                    avg10dCostList,
                    avg21dCostList,
                    avg62dCostList);
        } catch (Exception e) {
            log.warn("統計數據 股票代碼:{} 批次寫入db失敗 class:{} error:{}", stockcode, getClass().getName(), e.getMessage());
        }
    }

    public List<BigDecimal> countNdAvgList(List<BigDecimal> inputAvgList, int outputN) {
        if (outputN > inputAvgList.size()
                || outputN <= 0
        ) return new ArrayList<>();
        List<BigDecimal> avgNdCostList = new ArrayList<>();

        BigDecimal sum = new BigDecimal(0);
        for (int i = 0; i < outputN; i++) {
            sum = sum.add(inputAvgList.get(i));
        }
        avgNdCostList.add(sum.divide(new BigDecimal(outputN), 2, RoundingMode.HALF_UP));
        for (int i = outputN; i < inputAvgList.size(); i++) {
            sum = sum.add(inputAvgList.get(i)).subtract(inputAvgList.get(i - outputN));
            avgNdCostList.add(sum.divide(new BigDecimal(outputN), 2, RoundingMode.HALF_UP));
        }
        return avgNdCostList;
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveListData(String stockcode,
                             List<LocalDate> dateList,
                             List<BigDecimal> avgShareList,
                             List<BigDecimal> avg10dVolumeList,
                             List<BigDecimal> avg21dVolumeList,
                             List<BigDecimal> avgCostList,
                             List<BigDecimal> avg5dCostList,
                             List<BigDecimal> avg10dCostList,
                             List<BigDecimal> avg21dCostList,
                             List<BigDecimal> avg62dCostList) {
        synchronized (LocalcacheService.getStockcodeLock(stockcode)) {
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
                if (i < avg10dVolumeList.size()) stockStatistics.setAvg10dVolume(avg10dVolumeList.get(i));
                if (i < avg21dVolumeList.size()) stockStatistics.setAvg21dVolume(avg21dVolumeList.get(i));
                String joinKey = stockStatistics.getTradingDate().format(YMD) + stockcode;
                stockStatistics.setJoinKey(joinKey);
                stockStatisticsRepository.findByStockcodeAndTradingDate(stockcode, dateList.get(i)).ifPresentOrElse(
                        oldStockStatistics -> {
                            stockStatistics.setId(oldStockStatistics.getId());
                            stockStatisticsRepository.save(stockStatistics);
                        },
                        () -> {
                            stockStatisticsRepository.save(stockStatistics);
                        }
                );

                stockStatistics.setId(joinKey);
                stockStatisticsMapper.insertStockStatistics(stockStatistics);
            }
            log.info("統計數據 股票代碼:{} 寫入db完成", stockcode);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveListBatchData(String stockcode,
                                  List<LocalDate> dateList,
                                  List<BigDecimal> avgShareList,
                                  List<BigDecimal> avg10dVolumeList,
                                  List<BigDecimal> avg21dVolumeList,
                                  List<BigDecimal> avgCostList,
                                  List<BigDecimal> avg5dCostList,
                                  List<BigDecimal> avg10dCostList,
                                  List<BigDecimal> avg21dCostList,
                                  List<BigDecimal> avg62dCostList) {
        synchronized (LocalcacheService.getStockcodeLock(stockcode)) {
            Map<String, List<StockStatistics>> monthStatisticsMap = new ConcurrentHashMap<>();
            Map<String, List<StockStatistics>> calulateMonthStatisticsMap = new ConcurrentHashMap<>();
            List<LocalDate> monthList = dateList.stream().map(date -> LocalDate.of(date.getYear(), date.getMonthValue(), 1)).distinct().collect(Collectors.toList());
            monthList.forEach(localdate -> {
                monthStatisticsMap.put(localdate.format(YM), redisCacheUtility.readStockStatisticsMonthCache(stockcode, localdate.getYear(), localdate.getMonthValue()));
                calulateMonthStatisticsMap.put(localdate.format(YM), new ArrayList<>());
            });
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
                    if (i < avg10dVolumeList.size()) stockStatistics.setAvg10dVolume(avg10dVolumeList.get(i));
                    if (i < avg21dVolumeList.size()) stockStatistics.setAvg21dVolume(avg21dVolumeList.get(i));
                    String joinKey = stockStatistics.getTradingDate().format(YMD) + stockcode;
                    stockStatistics.setJoinKey(joinKey);

                    List<StockStatistics> redisStockStatisticsList = monthStatisticsMap.get(stockStatistics.getTradingDate().format(YM));
                    if (!redisStockStatisticsList.isEmpty()) {
                        StockStatistics redisStockStatistics = redisStockStatisticsList
                                .stream()
                                .filter(rss -> rss.getJoinKey().equals(joinKey))
                                .findFirst()
                                .get();
                        stockStatistics.setId(redisStockStatistics.getId());
                    }
                    calulateMonthStatisticsMap.get(stockStatistics.getTradingDate().format(YM)).add(stockStatistics);
                }
            } catch (Exception e) {
                log.warn("統計數據 股票代碼:{} ˇ轉換stockStatistics失敗 class:{} error:{}", stockcode, getClass().getName(), e.getMessage());
            }

            for (List<StockStatistics> resultList : calulateMonthStatisticsMap.values()) {
                stockStatisticsRepository.saveAll(resultList);
                resultList.forEach(stockStatistics -> stockStatistics.setId(stockStatistics.getJoinKey()));
                stockStatisticsMapper.insertStockStatisticsList(resultList);
            }
            log.info("統計數據 股票代碼:{} 批次寫入db完成", stockcode);

            monthList.forEach(localdate -> {
                redisCacheUtility.createStockStatisticsMonthCache(stockcode, localdate.getYear(), localdate.getMonthValue());
            });
            log.info("統計數據 股票代碼:{} 更新redis完成", stockcode);
        }
    }


    //GET
    public StockDetailStatistics getStockcodeStatistics(String stockcode, LocalDate date) {
        return StockDetailStatistics.convert(stockStatisticsRepository.findStockDetailStatisticsByStockcodeAndDate(stockcode, date).orElseGet(() -> new StockStatistics()));
    }

    public List<StockDetailStatistics> getStockcodeStatisticsList(String stockcode, int limit) {
        LocalDate temp = LocalDate.now();
        return getStockcodeStatisticsList(stockcode, temp.getYear(), temp.getMonthValue(), limit);
    }

    public List<StockDetailStatistics> getStockcodeStatisticsList(String stockcode, Integer year, int month, int limit) {
        LocalDate temp = LocalDate.of(year, month, 1);
        List<StockDetailStatistics> stockDetailStatisticsList = new ArrayList<>();
        int loopLimit = 0;
        while (stockDetailStatisticsList.size() < limit && loopLimit < limit / 20) {
            temp = temp.minusMonths(1);
            List<StockStatistics> stockStatisticsList = redisCacheUtility.readStockStatisticsMonthCache(stockcode, temp.getYear(), temp.getMonthValue());
            List<StockDetail> stockDetailList = redisCacheUtility.readStockDetailMonthCache(stockcode, temp.getYear(), temp.getMonthValue());
            stockDetailList = stockDetailList.stream().filter(stockDetail -> stockDetail.getPrice().compareTo(BigDecimal.ZERO) != 0).collect(Collectors.toList());
            if (stockStatisticsList.size() == stockDetailList.size()) {
                for (int i = 0; i < stockStatisticsList.size(); i++) {
                    stockStatisticsList.get(i).setStockDetail(Stream.of(stockDetailList.get(i)).collect(Collectors.toList()));
                }
            } else {
                log.warn("stockStatisticsList 與 stockDetailList 數量不一致 股票:{} 月份:{}", stockcode, temp.getMonthValue());
                stockDetailList = redisCacheUtility.createStockDetailMonthCache(stockcode, temp.getYear(), temp.getMonthValue());
                stockDetailList = stockDetailList.stream().filter(stockDetail -> stockDetail.getPrice().compareTo(BigDecimal.ZERO) != 0).collect(Collectors.toList());
                stockStatisticsList = redisCacheUtility.createStockStatisticsMonthCache(stockcode, temp.getYear(), temp.getMonthValue());
                if (stockStatisticsList.size() == stockDetailList.size()) {
                    for (int i = 0; i < stockStatisticsList.size(); i++) {
                        stockStatisticsList.get(i).setStockDetail(Stream.of(stockDetailList.get(i)).collect(Collectors.toList()));
                    }
                }
            }

            stockDetailStatisticsList.addAll(stockStatisticsList
                    .stream()
                    .map(StockDetailStatistics::convert)
                    .collect(Collectors.toList()));
            loopLimit += 1;
        }
        List<StockDetailStatistics> resultList = stockDetailStatisticsList.subList(0, limit);
        Collections.reverse(stockDetailStatisticsList.subList(0, limit));
        return resultList;
    }

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
            case d5:
                update.set(avg5d, result);
            case d10:
                update.set(avg10d, result);
            case d21:
                update.set(avg21d, result);
            case d62:
                update.set(avg62d, result);
        }
        mongoTemplate.findAndModify(query, update, StockStatistics.class, "stockStatistics");
        return true;
    }
}
