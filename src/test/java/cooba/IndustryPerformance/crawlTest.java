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
}
