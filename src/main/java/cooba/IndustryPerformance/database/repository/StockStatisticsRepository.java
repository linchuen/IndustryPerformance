package cooba.IndustryPerformance.database.repository;

import cooba.IndustryPerformance.database.entity.StockStatistics.StockStatistics;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockStatisticsRepository extends MongoRepository<StockStatistics, String>, StockStatisticsRepositoryCustom {
    List<StockStatistics> findByStockcode(String stockcode);

    Optional<StockStatistics> findByStockcodeAndTradingDate(String stockcode, LocalDate localDate);
}
