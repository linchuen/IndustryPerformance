package cooba.IndustryPerformance;

import cooba.IndustryPerformance.constant.RedisConstant;
import cooba.IndustryPerformance.database.entity.StockDetail.StockDetail;
import cooba.IndustryPerformance.service.CrawlerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class IndustryPerformanceApplicationTests {
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    RedisTemplate<String, Object> redisObjectTemplate;
    @Autowired
    CrawlerService crawlerService;

    @Test
    void Test() {
        StockDetail stockDetail = crawlerService.crawlStock("2330");
    }

    @Test
    void redisTest() {
        String stockcode = "2330";
        StockDetail stockDetail = crawlerService.crawlStock(stockcode);
        redisObjectTemplate.opsForValue().set(RedisConstant.STOCKDETAIL + LocalDate.now().toString() + ":" + stockcode, stockDetail, 90, TimeUnit.DAYS);
    }
}
