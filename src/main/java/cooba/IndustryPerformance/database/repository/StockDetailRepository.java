package cooba.IndustryPerformance.database.repository;

import cooba.IndustryPerformance.database.entity.StockDetail.StockDetail;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockDetailRepository extends MongoRepository<StockDetail, String> {
    @Query(value = "{$and: [ { stockcode:?0 },{ createdTime:{ $gt:?1 ,$lt:?2 } } ]}")
    Optional<List<StockDetail>> findByStockcodeAndCreatedTimeBetween(String stockcode, LocalDate starttime, LocalDate endtime);
}
