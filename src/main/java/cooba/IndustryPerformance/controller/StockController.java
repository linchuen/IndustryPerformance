package cooba.IndustryPerformance.controller;

import cooba.IndustryPerformance.database.entity.StockBasicInfo.StockBasicInfo;
import cooba.IndustryPerformance.database.entity.StockDetail.StockDetail;
import cooba.IndustryPerformance.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
public class StockController {
    @Autowired
    private StockService stockService;

    @GetMapping("stock/basicinfo/{stockCode}")
    public StockBasicInfo buildStockBasicInfo(@PathVariable String stockCode) {
        return stockService.buildStockBasicInfo(stockCode);
    }

    @GetMapping("stock/{stockCode}")
    public StockDetail getStockDetail(@PathVariable String stockCode) {
        return stockService.getStockDetailToday(stockCode)
                .orElseGet(() -> {
                    StockDetail stockDetail = stockService.buildStockDetail(stockCode);
                    if (stockDetail == null) {
                        StockBasicInfo stockBasicInfo = stockService.getStockBasicInfo(stockCode);
                        stockDetail = StockDetail.builder()
                                .stockcode(stockBasicInfo.getStockcode())
                                .name(stockBasicInfo.getName())
                                .companyType(stockBasicInfo.getCompanyType())
                                .industryType(stockBasicInfo.getIndustryType())
                                .build();
                        return stockDetail;
                    } else {
                        return stockDetail;
                    }
                });
    }

    @GetMapping("stock")
    public Set<String> getStockListByCompanyType(@RequestParam("companyType") String companyType) {
        return stockService.getStockSetByCompanyType(companyType);
    }

    @DeleteMapping("stock")
    public void deleteAllStockDetail() {
        stockService.deleteAllStockDetail();
    }
}
