package cooba.IndustryPerformance;

import cooba.IndustryPerformance.service.CrawlerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class stockTest {
    @Autowired
    private CrawlerService crawlerService;

    @Test
    public void Test() {
        crawlerService.crawlStock("2330");
    }
}
