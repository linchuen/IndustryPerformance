package cooba.IndustryPerformance.database.repository;

import cooba.IndustryPerformance.database.entity.StockStatistics.StockStatistics;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockStatisticsRepository extends MongoRepository<StockStatistics, String> {
    Optional<StockStatistics> findByStockcode(String stockcode);
}
