package cooba.IndustryPerformance;

import cooba.IndustryPerformance.database.entity.StockDetail.StockDetail;
import cooba.IndustryPerformance.database.repository.StockDetailRepository;
import cooba.IndustryPerformance.database.repository.StockStatisticsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;


@SpringBootTest
public class mongoTest {
    @Autowired
    MongoTemplate mongoTemplate;
    @Autowired
    StockDetailRepository stockDetailRepository;
    @Autowired
    StockStatisticsRepository stockStatisticsRepository;

    //@Test
    public void MongoTest() {
        Query query = new Query();
        query.addCriteria(Criteria.where("createdTime").gt(LocalDate.now()));
        List<StockDetail> stockDetailList = mongoTemplate.find(query, StockDetail.class, "stockDetail");
        List<LocalDate> dateList = stockDetailList.stream().map(stockDetail -> stockDetail.getCreatedTime()).distinct().collect(Collectors.toList());
        dateList.forEach(date -> {
            Query datequery = new Query().addCriteria(Criteria.where("createdTime").is(date));
            Update update = new Update().set("createdTime", date.minusYears(100));
            mongoTemplate.updateMulti(datequery, update, StockDetail.class, "stockDetail");
        });
    }

    @Test
    public void Test() {

        Query query = new Query();
        query.addCriteria(Criteria.where("stockcode").is("2330"));
        query.addCriteria(Criteria.where("createdTime").lt(LocalDate.now()));
        List<StockDetail> stockDetailList = mongoTemplate.find(query, StockDetail.class, "stockDetail");
        List<LocalDate> dateList = stockDetailList.stream().map(stockDetail -> stockDetail.getCreatedTime()).sorted().collect(Collectors.toList());
        dateList.forEach(System.out::println);
    }
}
