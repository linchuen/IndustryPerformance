package cooba.IndustryPerformance.database.mapper;

import cooba.IndustryPerformance.database.entity.StockDetail.StockDetail;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

public interface StockDetailMapper {
    void insertStockDetail(StockDetail stockDetail);

    void insertStockDetailList(@Param("list") List<StockDetail> stockDetailList);

    Long findStockDetail(@Param("stockcode") String stockcode, @Param("createdTime") LocalDate createdTime);
}
