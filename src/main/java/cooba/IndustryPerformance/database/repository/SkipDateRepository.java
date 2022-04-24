package cooba.IndustryPerformance.database.repository;

import cooba.IndustryPerformance.database.entity.SkipDate.SkipDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface SkipDateRepository extends JpaRepository<SkipDate, Integer> {
    Optional<SkipDate> findBySkipDate(LocalDate date);
}
