package cooba.IndustryPerformance.database.repository;

import cooba.IndustryPerformance.database.entity.TimeCounter.TimeCounter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(transactionManager = "txTransactionManager")
@Repository
public interface TimeCounterRepository extends JpaRepository<TimeCounter, Integer> {
}
