package cooba.IndustryPerformance.database.repository;

import cooba.IndustryPerformance.database.entity.BlackList;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BlackListReposiotry extends MongoRepository<BlackList, String> {
    Optional<BlackList> findByStockcode(String stockcode);
}
