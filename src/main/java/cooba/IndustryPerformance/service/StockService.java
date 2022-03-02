package cooba.IndustryPerformance.service;

import cooba.IndustryPerformance.constant.RedisConstant;
import cooba.IndustryPerformance.database.entity.StockDetail.StockDetail;
import cooba.IndustryPerformance.database.repository.StockDetailRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
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
    RedisTemplate redisTemplate;

    @Async("stockExecutor")
    public StockDetail asyncBuildStockDetail(String stockcode) {
        return buildStockDetail(stockcode);
    }

    public StockDetail buildStockDetail(String stockcode) {
        StockDetail stockDetail = crawlerService.crawlStock(stockcode);
        if (stockDetail == null) {
            return null;
        }

        if (!stockDetailRepository.findByStockcodeAndCreatedTime(stockcode, LocalDate.now()).isPresent()) {
            try {
                stockDetailRepository.save(stockDetail);
                log.info("股票代碼:{}成功建立", stockcode);
                return stockDetail;
            } catch (Exception e) {
                log.warn("股票代碼:{}建立失敗", stockcode);
                redisTemplate.opsForValue().set(RedisConstant.BLACKLIST + stockcode, stockcode, 3, TimeUnit.DAYS);
                return null;
            }
        } else {
            StockDetail oldstockDetail = stockDetailRepository.findByStockcodeAndCreatedTime(stockcode, LocalDate.now()).get();
            log.info("股票代碼:{}已經存在 ,\n舊資料:{}\n新資料:{}", stockcode, oldstockDetail, stockDetail);
            stockDetail.setId(oldstockDetail.getId());
            stockDetailRepository.save(stockDetail);
            log.info("股票代碼:{}成功更新", stockcode);
            return stockDetail;
        }
    }
    @Cacheable(value="users")
    public Optional<StockDetail> getStockDetailToday(String stockcode) {
        return stockDetailRepository.findByStockcodeAndCreatedTime(stockcode, LocalDate.now());
    }

    public Optional<StockDetail> getStockDetailLast_n_day(String stockcode,int days) {
        return stockDetailRepository.findByStockcodeAndCreatedTime(stockcode, LocalDate.now().minusDays(days));
    }

    public void deleteAllStockDetail() {
        stockDetailRepository.deleteAll();
    }
}
