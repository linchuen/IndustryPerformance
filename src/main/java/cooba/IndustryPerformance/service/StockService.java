package cooba.IndustryPerformance.service;

import cooba.IndustryPerformance.constant.RedisConstant;
import cooba.IndustryPerformance.database.entity.StockDetail.StockDetail;
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
    CrawlerService crawlerService;
    @Autowired
    LocalcacheService localcacheService;
    @Autowired
    RedisUtility redisUtility;

    private String today = LocalDate.now().toString();

    @Async("stockExecutor")
    public StockDetail asyncBuildStockDetail(String stockcode) {
        return buildStockDetail(stockcode);
    }

    public StockDetail buildStockDetail(String stockcode) {
        return stockDetailRepository.findByStockcodeAndCreatedTime(stockcode, LocalDate.now())
                .orElseGet(() -> {
                    synchronized (localcacheService.getStockcodeLock(stockcode)) {
                        StockDetail stockDetail = crawlerService.crawlStock(stockcode);
                        if (stockDetail == null) {
                            return null;
                        }
                        try {
                            stockDetailRepository.save(stockDetail);
                            log.info("股票代碼:{} 寫入mongo成功", stockcode);
                            redisUtility.valueObjectSet(RedisConstant.STOCKDETAIL + today + ":" + stockcode, stockDetail, 90, TimeUnit.DAYS);
                            return stockDetail;
                        } catch (ClassCastException classCastException) {
                            log.warn("股票代碼:{} 寫入redis失敗", stockcode);
                            return stockDetail;
                        } catch (Exception e) {
                            log.warn("股票代碼:{} 寫入mongo失敗", stockcode);
                            redisUtility.valueSet(RedisConstant.BLACKLIST + stockcode, stockcode, 3, TimeUnit.DAYS);
                            return null;
                        }
                    }
                });
    }

    public Optional<StockDetail> getStockDetailToday(String stockcode) {
        return getStockDetailLast_n_day(stockcode, 0);
    }

    public Optional<StockDetail> getStockDetailLast_n_day(String stockcode, int days) {
        LocalDate localDate = LocalDate.now().minusDays(days);
        if (redisUtility.hasKey(RedisConstant.STOCKDETAIL + localDate.toString() + ":" + stockcode)) {
            log.info("取得 StockDetail: {} redis資訊", stockcode);
            StockDetail stockDetail = (StockDetail) redisUtility.valueObjectGet(RedisConstant.STOCKDETAIL + today + ":" + stockcode, StockDetail.class);
            return Optional.of(stockDetail);
        } else {
            synchronized (localcacheService.getStockcodeLock(stockcode)) {
                if (redisUtility.hasKey(RedisConstant.STOCKDETAIL + localDate.toString() + ":" + stockcode)) {
                    log.info("取得 StockDetail: {} redis synchronized資訊", stockcode);
                    StockDetail stockDetail = (StockDetail) redisUtility.valueObjectGet(RedisConstant.STOCKDETAIL + today + ":" + stockcode, StockDetail.class);
                    return Optional.of(stockDetail);
                } else {
                    log.info("取得 StockDetail: {} mongo資訊", stockcode);
                    return stockDetailRepository.findByStockcodeAndCreatedTime(stockcode, localDate);
                }
            }
        }
    }

    public void deleteAllStockDetail() {
        stockDetailRepository.deleteAll();
    }
}
