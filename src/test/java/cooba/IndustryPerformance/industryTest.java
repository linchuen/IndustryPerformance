package cooba.IndustryPerformance;

import cooba.IndustryPerformance.enums.UrlEnum;
import cooba.IndustryPerformance.service.IndustryService;
import cooba.IndustryPerformance.service.StockService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StopWatch;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

@Slf4j
@SpringBootTest
public class industryTest {
    @Autowired
    private IndustryService industryService;
    @Autowired
    private StockService stockService;
    @Autowired
    RedisTemplate redisTemplate;

    @Test
    public void buildIndustryInfoTest() {
        industryService.biuldAllIndustryInfo();
        //industryService.buildIndustryInfo(UrlEnum.金融.name());
    }

    @Test
    public void getIndustryStockInfoTest() throws InterruptedException {
        StopWatch stopWatch = new StopWatch("IndustryStockInfo");
        //redisTemplate.delete(RedisConstant.INDUSTRYINFO + UrlEnum.交通航運.name());
        stopWatch.start("多執行緒測試");
        for (int i = 0; i < 50; i++) {
            CompletableFuture.supplyAsync(() -> industryService.getIndustryStockInfo(UrlEnum.金融.name()), Executors.newFixedThreadPool(5));
            CompletableFuture.supplyAsync(() -> industryService.getIndustryStockInfo(UrlEnum.金融.name()), Executors.newFixedThreadPool(5));
            Thread.sleep(1);
        }
        stopWatch.stop();
        //redisTemplate.delete(RedisConstant.INDUSTRYINFO + UrlEnum.交通航運.name());
        stopWatch.start("單執行緒測試");
        for (int i = 0; i < 100; i++) {
            industryService.getIndustryStockInfo(UrlEnum.金融.name());
        }
        stopWatch.stop();
        Thread.sleep(1000);
        log.info(stopWatch.prettyPrint());
    }

    //@Test
    public void buildIndustryStockDetailInfoTest() {
        //stockService.deleteAllStockDetail();
        StopWatch stopWatch = new StopWatch("Async");
        stopWatch.start();
        CompletableFuture.runAsync(() -> industryService.buildIndustryStockDetailInfo(UrlEnum.金融.name())).join();
        stopWatch.stop();
        log.info("Async 耗時:{}", stopWatch.getTotalTimeSeconds());
    }

    //@Test
    public void buildIndustryStockDetailInfoWithoutAsyncTest() {
        //stockService.deleteAllStockDetail();
        StopWatch stopWatch = new StopWatch("WithoutAsync");
        stopWatch.start();
        Map<String, String> industryStockMap = industryService.getIndustryStockInfo(UrlEnum.金融.name());
        if (industryStockMap.isEmpty()) {
            log.warn("industryStockMap 為空");
            return;
        }
        industryStockMap.forEach((k, v) -> stockService.buildStockDetail(k));
        stopWatch.stop();
        log.info("buildIndustryStockDetailInfo 成功 耗時:{}", stopWatch.getTotalTimeSeconds());
    }

    @Test
    public void getSubIndustryInfoTest() {
        System.out.println(industryService.getSubIndustryInfo(UrlEnum.金融.name()));
    }

    @Test
    public void getIndustryGrowthTest() {
        System.out.println(industryService.getIndustryGrowth(UrlEnum.金融.name()));
    }

    @Test
    public void getSubIndustryGrowthTest() {
        industryService.getSubIndustryInfo(UrlEnum.金融.name()).forEach(s -> industryService.getSubIndustryGrowth(UrlEnum.金融.name(), s));
    }
}
