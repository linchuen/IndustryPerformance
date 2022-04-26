package cooba.IndustryPerformance.controller;

import cooba.IndustryPerformance.database.entity.StockDetail.StockDetail;
import cooba.IndustryPerformance.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StockController {
    @Autowired
    private StockService stockService;

    @GetMapping("stock/{stockCode}")
    public StockDetail getStockDetail(@PathVariable String stockCode) {
        return stockService.getStockDetailToday(stockCode)
                .orElseGet(() -> {
                    StockDetail stockDetail = stockService.buildStockDetail(stockCode);
                    if (stockDetail == null) {
                        return new StockDetail();
                    } else {
                        return stockDetail;
                    }
                });
    }

    @DeleteMapping("stock")
    public void deleteAllStockDetail() {
        stockService.deleteAllStockDetail();
    }
}
