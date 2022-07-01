package cooba.IndustryPerformance;

import cooba.IndustryPerformance.enums.UrlEnum;
import cooba.IndustryPerformance.service.CrawlerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class crawlTest {
    @Autowired
    private CrawlerService crawlerService;

    @Test
    public void singletest() {
        crawlerService.crawlIndustry(UrlEnum.軟體服務.getUrl());
    }

    @Test
    public void crawlGoodInfoStockBasicInfo() {
        System.out.println(crawlerService.crawlGoodInfoStockBasicInfo("2330"));
    }

    @Test
    public void crawlStock() {
        System.out.println(crawlerService.crawlStock("2330"));
    }

    @Test
    public void crawlAnueSourceStock() {
        System.out.println(crawlerService.crawlAnueSourceStock("2330"));
    }

    @Test
    public void crawlGoodInfoSourceStock() {
        System.out.println(crawlerService.crawlGoodInfoSourceStock("2330"));
    }

    @Test
    public void crawlYahooSourceStock() {
        System.out.println(crawlerService.crawlYahooSourceStock("2330"));
    }

    @Test
    public void crawlStockBasicInfo() {
        System.out.println(crawlerService.crawlStockBasicInfo("2330"));
    }
}
