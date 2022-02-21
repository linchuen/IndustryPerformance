package cooba.IndustryPerformance.service;

import cooba.IndustryPerformance.database.entity.StockDetail.StockDetail;
import cooba.IndustryPerformance.database.repository.StockDetailRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Slf4j
@Service
public class StockService {
    @Autowired
    StockDetailRepository stockDetailRepository;
    @Autowired
    CrawlerService crawlerService;

    public StockDetail buildStockDetail(String stockcode) {
        StockDetail stockDetail = crawlerService.crawlStock(stockcode);

        if (!stockDetailRepository.findByStockcodeAndCreatedTime(stockcode, LocalDate.now()).isPresent()) {
            stockDetailRepository.save(stockDetail);
            log.info("股票代碼:{}成功建立", stockcode);
        } else {
            StockDetail oldstockDetail = stockDetailRepository.findByStockcodeAndCreatedTime(stockcode, LocalDate.now()).get();
            log.info("股票代碼:{}已經存在 ,\n舊資料:{}\n新資料:{}", stockcode, oldstockDetail, stockDetail);
            stockDetail.setId(oldstockDetail.getId());
            stockDetailRepository.save(stockDetail);
            log.info("股票代碼:{}成功更新", stockcode);
        }
        return stockDetail;
    }

    public Optional<StockDetail> getStockDetailLast_1_Day(String stockcode) {
        return stockDetailRepository.findByStockcodeAndCreatedTime(stockcode, LocalDate.now());
    }
}
