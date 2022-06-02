package cooba.IndustryPerformance.database.repository;

import cooba.IndustryPerformance.database.entity.StockStatistics.StockStatistics;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StockStatisticsRepositoryCustom {
    List<StockStatistics> findStockDetailStatisticsByStockcode(String stockcode, int limit);

    Optional<StockStatistics> findStockDetailStatisticsByStockcodeAndDate(String stockcode, LocalDate date);

    List<StockStatistics> findStockcodeByMonth(String stockcode, int year, int month);
}
