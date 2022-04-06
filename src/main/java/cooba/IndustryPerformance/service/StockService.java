package cooba.IndustryPerformance.service;

import cooba.IndustryPerformance.constant.RedisConstant;
import cooba.IndustryPerformance.database.entity.StockBasicInfo.StockBasicInfo;
import cooba.IndustryPerformance.database.entity.StockDetail.StockDetail;
import cooba.IndustryPerformance.database.repository.StockBasicInfoRepository;
import cooba.IndustryPerformance.database.repository.StockDetailRepository;
import cooba.IndustryPerformance.utility.RedisUtility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class StockService {
    @Autowired
    StockDetailRepository stockDetailRepository;
    @Autowired
    StockBasicInfoRepository stockBasicInfoRepository;
    @Autowired
    CrawlerService crawlerService;
    @Autowired
    RedisUtility redisUtility;

    private String today = LocalDate.now().toString();

    @Async("stockExecutor")
    public StockBasicInfo asyncBuildStockBasicInfo(String stockcode) {
        return buildStockBasicInfo(stockcode);
    }

    public StockBasicInfo buildStockBasicInfo(String stockcode) {
        synchronized (LocalcacheService.getStockcodeLock(stockcode)) {
            return stockBasicInfoRepository.findByStockcode(stockcode)
                    .orElseGet(() -> {
                        StockBasicInfo stockBasicInfo = crawlerService.crawlStockBasicInfo(stockcode);
                        if (stockBasicInfo == null) {
                            return null;
                        } else if (stockBasicInfo.getCompanyType().equals("興櫃")) {
                            redisUtility.valueSet(RedisConstant.BLACKLIST + stockcode, stockcode);
                        }
                        try {
                            stockBasicInfoRepository.save(stockBasicInfo);
                            log.info("股票基本資料: {} 寫入mongodb成功", stockcode);
                            return stockBasicInfo;
                        } catch (Exception e) {
                            log.warn("股票代碼:{} 寫入mongodb失敗", stockcode);
                            return null;
                        }
                    });
        }
    }

    @Async("stockExecutor")
    public StockDetail asyncBuildStockDetail(String stockcode) {
        return buildStockDetail(stockcode);
    }

    public StockDetail buildStockDetail(String stockcode) {
        synchronized (LocalcacheService.getStockcodeLock(stockcode)) {
            return stockDetailRepository.findByStockcodeAndCreatedTime(stockcode, LocalDate.now())
                    .orElseGet(() -> {
                        StockDetail stockDetail = crawlerService.crawlStock(stockcode);
                        if (stockDetail == null) {
                            return null;
                        }
                        try {
                            stockDetailRepository.save(stockDetail);
                            log.info("股票代碼:{} 寫入mongodb成功", stockcode);
                            redisUtility.valueObjectSet(RedisConstant.STOCKDETAIL + today + ":" + stockcode, stockDetail, 90, TimeUnit.DAYS);
                            return stockDetail;
                        } catch (Exception e) {
                            log.warn("股票代碼:{} 寫入mongodb失敗", stockcode);
                            redisUtility.valueSet(RedisConstant.BLACKLIST + stockcode, stockcode, 3, TimeUnit.DAYS);
                            return null;
                        }
                    });
        }
    }

    public Optional<StockDetail> getStockDetailToday(String stockcode) {
        return getStockDetailLast_n_day(stockcode, 0);
    }

    public Optional<StockDetail> getStockDetailLast_n_day(String stockcode, int days) {
        LocalDate localDate = LocalDate.now().minusDays(days);
        String key = RedisConstant.STOCKDETAIL + localDate.toString() + ":" + stockcode;

        if (redisUtility.hasKey(key)) {
            log.info("取得 StockDetail: {} redis資訊", stockcode);
            StockDetail stockDetail = (StockDetail) redisUtility.valueObjectGet(key, StockDetail.class);
            return Optional.of(stockDetail);
        } else {
            synchronized (LocalcacheService.getStockcodeLock(stockcode)) {
                if (redisUtility.hasKey(key)) {
                    log.info("取得 StockDetail: {} redis synchronized資訊", stockcode);
                    StockDetail stockDetail = (StockDetail) redisUtility.valueObjectGet(key, StockDetail.class);
                    return Optional.of(stockDetail);
                } else {
                    log.info("取得 StockDetail: {} mongodb資訊", stockcode);
                    Optional<StockDetail> stockDetailOptional = stockDetailRepository.findByStockcodeAndCreatedTime(stockcode, localDate);
                    stockDetailOptional.ifPresent(stockDetail -> redisUtility.valueObjectSet(key, stockDetail));
                    return stockDetailOptional;
                }
            }
        }
    }

    public void deleteAllStockDetail() {
        stockDetailRepository.deleteAll();
    }
}
