package cooba.IndustryPerformance.utility;

import com.fasterxml.jackson.core.type.TypeReference;
import cooba.IndustryPerformance.constant.RedisConstant;
import cooba.IndustryPerformance.database.entity.EvaluateEntity.EvaluateEntity;
import cooba.IndustryPerformance.database.entity.StockBasicInfo.StockBasicInfo;
import cooba.IndustryPerformance.database.entity.StockDetail.StockDetail;
import cooba.IndustryPerformance.database.entity.StockStatistics.StockStatistics;
import cooba.IndustryPerformance.database.repository.EvaluateEntityRepository;
import cooba.IndustryPerformance.database.repository.StockDetailRepository;
import cooba.IndustryPerformance.database.repository.StockStatisticsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static cooba.IndustryPerformance.constant.CommonConstant.YM;
import static cooba.IndustryPerformance.constant.RedisConstant.*;

@Component
public class RedisCacheUtility {
    @Autowired
    StockStatisticsRepository stockStatisticsRepository;
    @Autowired
    StockDetailRepository stockDetailRepository;
    @Autowired
    EvaluateEntityRepository evaluateEntityRepository;
    @Autowired
    RedisUtility redisUtility;

    public List<StockDetail> createStockDetailMonthCache(String stockcode, int year, int month) {
        LocalDate date = LocalDate.of(year, month, 1);
        List<StockDetail> stockDetailList = stockDetailRepository.findStockcodeByMonth(stockcode, year, month);
        if (date.isBefore(LocalDate.now().minusMonths(6))) {
            return stockDetailList;
        }
        if (!stockDetailList.isEmpty()) {
            redisUtility.valueObjectSet(STOCKDETAILLIST + year + "_" + month + ":" + stockcode, stockDetailList, 30, TimeUnit.DAYS);
        }
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
        if (!stockStatisticsList.isEmpty()) {
            redisUtility.valueObjectSet(STOCKSTATISTICSLIST + year + "_" + month + ":" + stockcode, stockStatisticsList, 30, TimeUnit.DAYS);
        }
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

    public List<EvaluateEntity> createEvaluateEntityMonthCache(int year, int month) {
        LocalDate date = LocalDate.of(year, month, 1);
        List<EvaluateEntity> evaluateEntityList = evaluateEntityRepository.findByDateStr(date.format(YM));
        if (date.isBefore(LocalDate.now().minusMonths(6))) {
            return evaluateEntityList;
        }
        if (!evaluateEntityList.isEmpty()) {
            redisUtility.valueObjectSet(EVALUATEENTITYLIST + year + "_" + month, evaluateEntityList, 30, TimeUnit.DAYS);
        }
        return evaluateEntityList;
    }

    public List<EvaluateEntity> readEvaluateEntityMonthCache(int year, int month) {
        List<EvaluateEntity> evaluateEntityList = (List<EvaluateEntity>) redisUtility.valueObjectGet(EVALUATEENTITYLIST + year + "_" + month, new TypeReference<List<EvaluateEntity>>() {
        });
        if (evaluateEntityList == null || evaluateEntityList.isEmpty()) {
            evaluateEntityList = createEvaluateEntityMonthCache(year, month);
        }
        return evaluateEntityList;
    }

    public StockBasicInfo readStockBasicInfoCache(String stockcode) {
        return (StockBasicInfo) redisUtility.valueObjectGet(RedisConstant.STOCKBASICINFO + stockcode, StockBasicInfo.class);
    }
}
