package cooba.IndustryPerformance.service;

import cooba.IndustryPerformance.database.entity.StockDetail.StockDetail;
import cooba.IndustryPerformance.database.repository.StockDetailRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class StockService {
    @Autowired
    StockDetailRepository stockDetailRepository;
    @Autowired
    CrawlerService crawlerService;

    public void buildStockDetail(String stockcode) {
        StockDetail stockDetail = crawlerService.crawlStock(stockcode);
        stockDetailRepository.save(stockDetail);
    }
}
