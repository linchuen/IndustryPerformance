package cooba.IndustryPerformance.database.repository;

import cooba.IndustryPerformance.database.entity.StockDetail.StockDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDate;
import java.util.List;

public class StockDetailRepositoryCustomImpl implements StockDetailRepositoryCustom {
    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public List<StockDetail> findStockcodeByMonth(String stockcode, int year, int month) {
        LocalDate firstDay = LocalDate.of(year, month, 1);
        LocalDate lastDay = firstDay.withDayOfMonth(firstDay.getMonth().length(firstDay.isLeapYear()));

        Query query = new Query();
        query.addCriteria(Criteria.where("stockcode").is(stockcode));
        query.addCriteria(Criteria.where("createdTime").gte(firstDay).andOperator(Criteria.where("createdTime").lte(lastDay)));
        query.with(Sort.by(Sort.Direction.DESC, "createdTime"));
        List<StockDetail> stockDetailList = mongoTemplate.find(query, StockDetail.class, "stockDetail");
        return stockDetailList;
    }
}
