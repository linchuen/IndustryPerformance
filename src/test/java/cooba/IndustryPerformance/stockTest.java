package cooba.IndustryPerformance;

import cooba.IndustryPerformance.service.StockService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class stockTest {
    @Autowired
    private StockService stockService;

    @Test
    public void Test() {
        stockService.buildStockDetail("2330");
    }
}
