package cooba.IndustryPerformance.database.mapper;

import cooba.IndustryPerformance.database.entity.StockDetail.StockDetail;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;

public interface StockDetailMapper {
    void insertStockDetail(StockDetail stockDetail);

    Long findStockDetail(@Param(value = "stockcode") String stockcode, @Param(value = "createdTime") LocalDate createdTime);
}
