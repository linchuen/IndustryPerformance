package cooba.IndustryPerformance.service;

import cooba.IndustryPerformance.database.entity.BlackList;
import cooba.IndustryPerformance.database.entity.StockDetail.StockDetail;
import cooba.IndustryPerformance.database.repository.BlackListReposiotry;
import cooba.IndustryPerformance.database.repository.StockDetailRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
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
    @Autowired
    BlackListReposiotry blackListReposiotry;

    @Async("stockExecutor")
    public StockDetail asyncBuildStockDetail(String stockcode) {
        return buildStockDetail(stockcode);
    }

    public StockDetail buildStockDetail(String stockcode) {
        StockDetail stockDetail = crawlerService.crawlStock(stockcode);
        if (stockDetail == null) {
            return null;
        }

        if (!stockDetailRepository.findByStockcodeAndCreatedTime(stockcode, LocalDate.now()).isPresent()) {
            try {
                stockDetailRepository.save(stockDetail);
                log.info("股票代碼:{}成功建立", stockcode);
                return stockDetail;
            } catch (Exception e) {
                log.warn("股票代碼:{}建立失敗", stockcode);
                BlackList blackList = BlackList.builder()
                        .stockcode(stockcode)
                        .build();
                if (!blackListReposiotry.findByStockcode(stockcode).isPresent()) {
                    blackListReposiotry.save(blackList);
                }
                return null;
            }
        } else {
            StockDetail oldstockDetail = stockDetailRepository.findByStockcodeAndCreatedTime(stockcode, LocalDate.now()).get();
            log.info("股票代碼:{}已經存在 ,\n舊資料:{}\n新資料:{}", stockcode, oldstockDetail, stockDetail);
            stockDetail.setId(oldstockDetail.getId());
            stockDetailRepository.save(stockDetail);
            log.info("股票代碼:{}成功更新", stockcode);
            return stockDetail;
        }
    }

    public Optional<StockDetail> getStockDetailLast_1_Day(String stockcode) {
        return stockDetailRepository.findByStockcodeAndCreatedTime(stockcode, LocalDate.now());
    }

    public void deleteAllStockDetail() {
        stockDetailRepository.deleteAll();
    }
}
