package cooba.IndustryPerformance.database.repository;

import cooba.IndustryPerformance.database.entity.StockDetail.StockDetail;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockDetailRepository extends MongoRepository<StockDetail, String> {
    Optional<StockDetail> findByStockcodeAndCreatedTime(String stockcode, LocalDate localDate);

    void deleteByStockcodeAndCreatedTime(String stockcode, LocalDate localDate);

    Optional<StockDetail> findByStockcodeAndCompanyTypeAndCreatedTime(String stockcode, String companyType, LocalDate localDate);

    List<StockDetail> findByCompanyType(String companyType);

    void deleteByCompanyType(String companyType);
}
