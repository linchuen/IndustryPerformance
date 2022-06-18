package cooba.IndustryPerformance.entity;

import cooba.IndustryPerformance.database.entity.StockStatistics.StockStatistics;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class StockDetailStatistics {
    private String 股票代碼;
    private String 名稱;
    private BigDecimal 收盤;
    private BigDecimal 開盤;
    private BigDecimal 最高;
    private BigDecimal 最低;
    private BigDecimal 平均成本;
    private BigDecimal 平均5日成本;
    private BigDecimal 平均10日成本;
    private BigDecimal 平均21日成本;
    private BigDecimal 平均62日成本;
    private BigDecimal 平均股數;
    private int 成交筆數;
    private int 平均10日成交筆數;
    private int 平均21日成交筆數;
    private LocalDate tradingDate;

    public static StockDetailStatistics convert(StockStatistics stockStatistics) {
        if (stockStatistics.getStockDetail().isEmpty()) return new StockDetailStatistics();
        return StockDetailStatistics.builder()
                .股票代碼(stockStatistics.getStockcode())
                .名稱(stockStatistics.getStockDetail().get(0).getName())
                .開盤(stockStatistics.getStockDetail().get(0).getOpen())
                .收盤(stockStatistics.getStockDetail().get(0).getLastprice())
                .最高(stockStatistics.getStockDetail().get(0).getHighest())
                .最低(stockStatistics.getStockDetail().get(0).getLowest())
                .平均成本(stockStatistics.getAvgCost())
                .平均5日成本(stockStatistics.getAvg5dCost() == null ? BigDecimal.valueOf(0) : stockStatistics.getAvg5dCost())
                .平均10日成本(stockStatistics.getAvg10dCost() == null ? BigDecimal.valueOf(0) : stockStatistics.getAvg10dCost())
                .平均21日成本(stockStatistics.getAvg21dCost() == null ? BigDecimal.valueOf(0) : stockStatistics.getAvg21dCost())
                .平均62日成本(stockStatistics.getAvg62dCost() == null ? BigDecimal.valueOf(0) : stockStatistics.getAvg62dCost())
                .平均股數(stockStatistics.getAvgShare())
                .成交筆數(stockStatistics.getStockDetail().get(0).getTradingVolume())
                .平均10日成交筆數(stockStatistics.getAvg10dVolume() == null ? 0 : stockStatistics.getAvg10dVolume().intValue())
                .平均21日成交筆數(stockStatistics.getAvg21dVolume() == null ? 0 : stockStatistics.getAvg21dVolume().intValue())
                .tradingDate(stockStatistics.getTradingDate())
                .build();
    }
}
