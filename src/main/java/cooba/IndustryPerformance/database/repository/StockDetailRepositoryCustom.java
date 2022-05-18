package cooba.IndustryPerformance.database.repository;

import cooba.IndustryPerformance.database.entity.StockDetail.StockDetail;

import java.util.List;

public interface StockDetailRepositoryCustom {
    List<StockDetail> findStockDetailStatisticsByStockcodeAndDate(String stockcode);
}
