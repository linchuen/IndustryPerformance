package cooba.IndustryPerformance.database.repository;

import cooba.IndustryPerformance.database.entity.Industry.Industry;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IndustryRepository extends MongoRepository<Industry, String> {
    Optional<Industry> findByIndustryName(String industryName);
}
