package cooba.IndustryPerformance.service;

import cooba.IndustryPerformance.constant.RedisConstant;
import cooba.IndustryPerformance.database.entity.StockDetail.StockDetail;
import cooba.IndustryPerformance.database.repository.StockDetailRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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
    RedisTemplate redisTemplate;

    @Async("stockExecutor")
    public StockDetail asyncBuildStockDetail(String stockcode) {
        return buildStockDetail(stockcode);
    }

    public StockDetail buildStockDetail(String stockcode) {
        if (!stockDetailRepository.findByStockcodeAndCreatedTime(stockcode, LocalDate.now()).isPresent()) {
            StockDetail stockDetail = crawlerService.crawlStock(stockcode);
            if (stockDetail == null) {
                return null;
            }
            try {
                stockDetailRepository.save(stockDetail);
                log.info("股票代碼:{}成功建立", stockcode);
                redisTemplate.opsForValue().set(RedisConstant.STOCKDETAIL + LocalDate.now().toString() + ":" + stockcode, stockDetail, 90, TimeUnit.DAYS);
                return stockDetail;
            } catch (Exception e) {
                log.warn("股票代碼:{}建立失敗", stockcode);
                redisTemplate.opsForValue().set(RedisConstant.BLACKLIST + stockcode, stockcode, 3, TimeUnit.DAYS);
                return null;
            }
        } else {
            StockDetail oldstockDetail = stockDetailRepository.findByStockcodeAndCreatedTime(stockcode, LocalDate.now()).get();
            log.info("股票代碼:{}已經存在 ,舊資料:{}", stockcode, oldstockDetail);
            return oldstockDetail;
        }
    }

    public Optional<StockDetail> getStockDetailToday(String stockcode) {
        return getStockDetailLast_n_day(stockcode, 0);
    }

    public Optional<StockDetail> getStockDetailLast_n_day(String stockcode, int days) {
        LocalDate localDate = LocalDate.now().minusDays(days);
        if (redisTemplate.hasKey(RedisConstant.STOCKDETAIL + localDate.toString() + ":" + stockcode)) {
            log.info("已從redis取得股票 {} 資訊", stockcode);
            return Optional.of((StockDetail) redisTemplate.boundValueOps(RedisConstant.STOCKDETAIL + LocalDate.now().toString() + ":" + stockcode).get());
        } else {
            synchronized (localcacheService.getStockcodeLock(stockcode)) {
                if (redisTemplate.hasKey(RedisConstant.STOCKDETAIL + localDate.toString() + ":" + stockcode)) {
                    log.info("已從mongo新增redis並取得股票 {} 資訊", stockcode);
                    return Optional.of((StockDetail) redisTemplate.boundValueOps(RedisConstant.STOCKDETAIL + localDate.toString() + ":" + stockcode).get());
                } else {
                    log.info("已從mongo取得股票 {} 資訊", stockcode);
                    return stockDetailRepository.findByStockcodeAndCreatedTime(stockcode, localDate);
                }
            }
        }
    }

    public void deleteAllStockDetail() {
        stockDetailRepository.deleteAll();
    }
}
