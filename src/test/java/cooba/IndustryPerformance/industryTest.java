package cooba.IndustryPerformance;

import cooba.IndustryPerformance.constant.UrlEnum;
import cooba.IndustryPerformance.service.IndustryService;
import cooba.IndustryPerformance.service.StockService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import java.util.Map;

@Slf4j
@SpringBootTest
public class industryTest {
    @Autowired
    private IndustryService industryService;
    @Autowired
    private StockService stockService;

    @Test
    public void getIndustryStockInfoTest() {
        industryService.getIndustryStockInfo(UrlEnum.金融.name());
    }

    @Test
    public void buildIndustryStockDetailInfoTest() {
        stockService.deleteAllStockDetail();
        StopWatch stopWatch = new StopWatch("Async");
        stopWatch.start();
        industryService.buildIndustryStockDetailInfo(UrlEnum.醫療器材.name());
        stopWatch.stop();
        log.info("Async 耗時:{}", stopWatch.getTotalTimeSeconds());
    }

    @Test
    public void buildIndustryStockDetailInfoWithoutAsyncTest() {
        stockService.deleteAllStockDetail();
        StopWatch stopWatch = new StopWatch("WithoutAsync");
        stopWatch.start();
        Map<String, String> industryStockMap = industryService.getIndustryStockInfo(UrlEnum.醫療器材.name());
        if (industryStockMap.isEmpty()) {
            log.warn("industryStockMap 為空");
            return;
        }
        industryStockMap.forEach((k, v) -> stockService.buildStockDetail(k));
        stopWatch.stop();
        log.info("buildIndustryStockDetailInfo 成功 耗時:{}", stopWatch.getTotalTimeSeconds());
    }

    @Test
    public void buildIndustryInfoTest() {
        //industryService.biuldAllIndustryInfo();
        industryService.buildIndustryInfo(UrlEnum.金融.name());
    }

    @Test
    public void getIndustryGrowthTest() {
        System.out.println(industryService.getIndustryGrowth(UrlEnum.金融.name()));
    }
}
