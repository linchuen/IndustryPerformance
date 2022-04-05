package cooba.IndustryPerformance.database.repository;

import cooba.IndustryPerformance.database.entity.StockBasicInfo.StockBasicInfo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockBasicInfoRepository extends MongoRepository<StockBasicInfo, String> {
    Optional<StockBasicInfo> findByStockcode(String stockcode);
}
