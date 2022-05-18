package cooba.IndustryPerformance.database.repository;

import cooba.IndustryPerformance.database.entity.StockDetail.StockDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.List;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;

public class StockDetailRepositoryCustomImpl implements StockDetailRepositoryCustom {
    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public List<StockDetail> findStockDetailStatisticsByStockcodeAndDate(String stockcode) {
        Aggregation aggregation = newAggregation(
                Aggregation.lookup("stockStatistics", "joinKey", "joinKey", "stockStatistics")
                , Aggregation.match(Criteria.where("stockcode").is("2330"))
        );
        AggregationResults aggregationResults = mongoTemplate.aggregate(aggregation, "stockDetail", StockDetail.class);
        return aggregationResults.getMappedResults();
    }
}
