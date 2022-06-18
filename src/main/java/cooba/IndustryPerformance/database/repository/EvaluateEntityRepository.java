package cooba.IndustryPerformance.database.repository;

import cooba.IndustryPerformance.database.entity.EvaluateEntity.EvaluateEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvaluateEntityRepository extends MongoRepository<EvaluateEntity, String> {
    List<EvaluateEntity> findByStockcode(String stockcode);

    Optional<EvaluateEntity> findByStockcodeAndDateStr(String stockcode, String dateStr);
}
