package cooba.IndustryPerformance;

import cooba.IndustryPerformance.database.entity.StockDetail.StockDetail;
import cooba.IndustryPerformance.service.CrawlerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
class IndustryPerformanceApplicationTests {
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    CrawlerService crawlerService;

    @Test
    void Test() {
        StockDetail stockDetail = crawlerService.crawlStock("2330");
        redisTemplate.opsForValue().set("test", stockDetail);
        StockDetail restockDetail = (StockDetail) redisTemplate.opsForValue().get("test");
        System.out.println(redisTemplate.opsForValue().get("test").getClass());
        System.out.println(restockDetail.getName() + restockDetail.getPrice());
    }

}
