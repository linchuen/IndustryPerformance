package cooba.IndustryPerformance.controller;

import cooba.IndustryPerformance.database.entity.StockDetail.StockDetail;
import cooba.IndustryPerformance.service.CrawlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CrawlerController {
    @Autowired
    CrawlerService crawlerService;

    @GetMapping("crawler/{stockcode}")
    public StockDetail crawlStock(@PathVariable String stockcode) {
        return crawlerService.crawlStock(stockcode);
    }
}
