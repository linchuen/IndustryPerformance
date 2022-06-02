package cooba.IndustryPerformance.database.repository;

import cooba.IndustryPerformance.database.entity.StockDetail.StockDetail;

import java.util.List;

public interface StockDetailRepositoryCustom {
    List<StockDetail> findStockcodeByMonth(String stockcode, int year, int month);
}
