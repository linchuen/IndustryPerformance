package cooba.IndustryPerformance.database.mapper;

import cooba.IndustryPerformance.database.entity.StockStatistics.StockStatistics;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface StockStatisticsMapper {
    void insertStockStatistics(StockStatistics stockStatistics);

    void insertStockStatisticsList(@Param("list") List<StockStatistics> stockStatisticsList);
}
