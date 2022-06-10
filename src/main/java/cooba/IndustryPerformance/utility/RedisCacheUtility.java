package cooba.IndustryPerformance.utility;

import com.fasterxml.jackson.core.type.TypeReference;
import cooba.IndustryPerformance.database.entity.StockDetail.StockDetail;
import cooba.IndustryPerformance.database.entity.StockStatistics.StockStatistics;
import cooba.IndustryPerformance.database.repository.StockDetailRepository;
import cooba.IndustryPerformance.database.repository.StockStatisticsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static cooba.IndustryPerformance.constant.RedisConstant.STOCKDETAILLIST;
import static cooba.IndustryPerformance.constant.RedisConstant.STOCKSTATISTICSLIST;

@Component
public class RedisCacheUtility {
    @Autowired
    StockStatisticsRepository stockStatisticsRepository;
    @Autowired
    StockDetailRepository stockDetailRepository;
    @Autowired
    RedisUtility redisUtility;

    public List<StockDetail> createStockDetailMonthCache(String stockcode, int year, int month) {
        LocalDate date = LocalDate.of(year, month, 1);
        List<StockDetail> stockDetailList = stockDetailRepository.findStockcodeByMonth(stockcode, year, month);
        if (date.isBefore(LocalDate.now().minusMonths(6))) {
            return stockDetailList;
        }
        redisUtility.valueObjectSet(STOCKDETAILLIST + year + "_" + month + ":" + stockcode, stockDetailList, 30, TimeUnit.DAYS);
        return stockDetailList;
    }


    public List<StockDetail> readStockDetailMonthCache(String stockcode, int year, int month) {
        List<StockDetail> stockDetailList = (List<StockDetail>) redisUtility.valueObjectGet(STOCKDETAILLIST + year + "_" + month + ":" + stockcode, new TypeReference<List<StockDetail>>() {
        });
        if (stockDetailList == null || stockDetailList.isEmpty()) {
            stockDetailList = createStockDetailMonthCache(stockcode, year, month);
        }
        return stockDetailList;
    }

    public List<StockStatistics> createStockStatisticsMonthCache(String stockcode, int year, int month) {
        LocalDate date = LocalDate.of(year, month, 1);
        List<StockStatistics> stockStatisticsList = stockStatisticsRepository.findStockcodeByMonth(stockcode, year, month);
        if (date.isBefore(LocalDate.now().minusMonths(6))) {
            return stockStatisticsList;
        }
        redisUtility.valueObjectSet(STOCKSTATISTICSLIST + year + "_" + month + ":" + stockcode, stockStatisticsList, 30, TimeUnit.DAYS);
        return stockStatisticsList;
    }

    public List<StockStatistics> readStockStatisticsMonthCache(String stockcode, int year, int month) {
        List<StockStatistics> stockStatisticsList = (List<StockStatistics>) redisUtility.valueObjectGet(STOCKSTATISTICSLIST + year + "_" + month + ":" + stockcode, new TypeReference<List<StockStatistics>>() {
        });
        if (stockStatisticsList == null || stockStatisticsList.isEmpty()) {
            stockStatisticsList = createStockStatisticsMonthCache(stockcode, year, month);
        }
        return stockStatisticsList;
    }
}
