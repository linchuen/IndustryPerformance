package cooba.IndustryPerformance.controller;

import cooba.IndustryPerformance.database.entity.StockDetail.StockDetail;
import cooba.IndustryPerformance.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("stock")
    public List<String> getStockListByCompanyType(@RequestParam("companyType") String companyType) {
        return stockService.getStockListByCompanyType(companyType);
    }

    @DeleteMapping("stock")
    public void deleteAllStockDetail() {
        stockService.deleteAllStockDetail();
    }
}
