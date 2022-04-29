package cooba.IndustryPerformance;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cooba.IndustryPerformance.database.entity.StockDetail.StockDetail;
import cooba.IndustryPerformance.database.repository.StockDetailRepository;
import cooba.IndustryPerformance.service.StockService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

@SpringBootTest
public class stockTest {
    @Autowired
    private StockService stockService;
    @Autowired
    private StockDetailRepository stockDetailRepository;
    @Autowired
    ObjectMapper objectMapper;

    @Test
    public void Test() throws JsonProcessingException {
        StockDetail stockDetail = stockService.buildStockDetail("2330");
        System.out.println(objectMapper.writeValueAsString(stockDetail));
    }

    @Test
    public void getStockDetailLast_n_day() throws JsonProcessingException {
        StockDetail stockDetail = stockService.getStockDetailLast_n_day("2330", 1).get();
        System.out.println(objectMapper.writeValueAsString(stockDetail));
    }

    @Test
    public void test() throws JsonProcessingException {
        LocalDate date = LocalDate.of(2022, 4, 14);
        StockDetail stockDetail = stockDetailRepository.findByStockcodeAndCreatedTime("2330", date).get();
        System.out.println(objectMapper.writeValueAsString(stockDetail));
    }


}
