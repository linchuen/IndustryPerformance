package cooba.IndustryPerformance;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cooba.IndustryPerformance.database.entity.StockDetail.StockDetail;
import cooba.IndustryPerformance.service.StockService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class stockTest {
    @Autowired
    private StockService stockService;
    @Autowired
    ObjectMapper objectMapper;

    @Test
    public void Test() throws JsonProcessingException {
        StockDetail stockDetail = stockService.buildStockDetail("2330");
        System.out.println(objectMapper.writeValueAsString(stockDetail));
    }
}
