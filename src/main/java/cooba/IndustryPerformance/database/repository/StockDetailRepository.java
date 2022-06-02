package cooba.IndustryPerformance.database.repository;

import cooba.IndustryPerformance.database.entity.StockDetail.StockDetail;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockDetailRepository extends MongoRepository<StockDetail, String>, StockDetailRepositoryCustom {
    Optional<StockDetail> findByStockcodeAndCreatedTime(String stockcode, LocalDate localDate);

    List<StockDetail> findByStockcodeAndCreatedTimeBetweenOrderByCreatedTimeDesc(String stockcode, LocalDate startDate, LocalDate endDate);

    List<StockDetail> findByStockcodeAndCreatedTimeBeforeOrderByCreatedTimeDesc(String stockcode, LocalDate startDate);

    List<StockDetail> findTop100ByStockcodeAndCreatedTimeBeforeOrderByCreatedTimeDesc(String stockcode, LocalDate startDate);

    List<StockDetail> deleteByCreatedTime(LocalDate date);
}
