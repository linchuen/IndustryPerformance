package cooba.IndustryPerformance.database.repository;

import cooba.IndustryPerformance.database.entity.StockDetail.StockDetail;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface StockDetailRepository extends MongoRepository<StockDetail, String> {
    Optional<StockDetail> findByStockcodeAndCreatedTime(String stockcode, LocalDate localDate);
}
