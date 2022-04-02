package cooba.IndustryPerformance;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    @Autowired
    ObjectMapper objectMapper;

    @Test
    void Test() {
        StockDetail stockDetail = crawlerService.crawlStock("2330");
    }

    //@Test
    void redisTest() throws JsonProcessingException {
        String stockcode = "2330";
        StockDetail stockDetail = crawlerService.crawlStock(stockcode);
        redisObjectTemplate.opsForValue().set(RedisConstant.STOCKDETAIL + LocalDate.now().toString() + ":" + stockcode, stockDetail, 90, TimeUnit.DAYS);
        redisTemplate.opsForValue().set(RedisConstant.STOCKDETAIL + LocalDate.now().toString() + "::" + stockcode, objectMapper.writeValueAsString(stockDetail), 90, TimeUnit.DAYS);
        System.out.println(redisObjectTemplate.opsForValue().get("stockDetail:2022-04-01:2330"));
        System.out.println(redisObjectTemplate.opsForValue().get("stockDetail:2022-04-01:2330").getClass());
        String s = String.valueOf(redisTemplate.opsForValue().get("stockDetail:2022-04-01::2330"));
        System.out.println(redisTemplate.opsForValue().get("stockDetail:2022-04-01::2330"));
        StockDetail stockDetail1 = objectMapper.readValue(s, StockDetail.class);
        System.out.println(stockDetail1.getName());
    }
}
