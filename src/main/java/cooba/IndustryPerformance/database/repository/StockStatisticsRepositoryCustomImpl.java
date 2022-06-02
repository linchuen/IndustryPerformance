package cooba.IndustryPerformance.database.repository;

import cooba.IndustryPerformance.database.entity.StockStatistics.StockStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;

public class StockStatisticsRepositoryCustomImpl implements StockStatisticsRepositoryCustom {
    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public List<StockStatistics> findStockDetailStatisticsByStockcode(String stockcode, int n) {
        Aggregation aggregation = newAggregation(
                Aggregation.lookup("stockDetail", "joinKey", "joinKey", "stockDetail")
                , Aggregation.match(Criteria.where("stockcode").is(stockcode))
                , Aggregation.sort(Sort.by(Sort.Direction.DESC, "joinKey"))
                , Aggregation.limit(n)
        );
        AggregationResults aggregationResults = mongoTemplate.aggregate(aggregation, "stockStatistics", StockStatistics.class);
        return aggregationResults.getMappedResults();
    }

    @Override
    public Optional<StockStatistics> findStockDetailStatisticsByStockcodeAndDate(String stockcode, LocalDate date) {
        Aggregation aggregation = newAggregation(
                Aggregation.lookup("stockDetail", "joinKey", "joinKey", "stockDetail")
                , Aggregation.match(Criteria.where("stockcode").is(stockcode)
                        .and("tradingDate").is(date))
        );
        AggregationResults aggregationResults = mongoTemplate.aggregate(aggregation, "stockStatistics", StockStatistics.class);
        return (Optional<StockStatistics>) aggregationResults.getUniqueMappedResult();
    }

    @Override
    public List<StockStatistics> findStockcodeByMonth(String stockcode, int year, int month) {
        LocalDate firstDay = LocalDate.of(year, month, 1);
        LocalDate lastDay = firstDay.withDayOfMonth(firstDay.getMonth().length(firstDay.isLeapYear()));

        Query query = new Query();
        query.addCriteria(Criteria.where("stockcode").is(stockcode));
        query.addCriteria(Criteria.where("tradingDate").gte(firstDay).andOperator(Criteria.where("tradingDate").lte(lastDay)));
        query.with(Sort.by(Sort.Direction.DESC, "tradingDate"));
        List<StockStatistics> stockStatisticsList = mongoTemplate.find(query, StockStatistics.class, "stockStatistics");
        return stockStatisticsList;
    }
}
