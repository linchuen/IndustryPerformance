package cooba.IndustryPerformance.service;

import cooba.IndustryPerformance.constant.UrlEnum;
import cooba.IndustryPerformance.database.entity.Industry.Industry;
import cooba.IndustryPerformance.database.entity.Industry.SubIndustry;
import cooba.IndustryPerformance.database.entity.StockDetail.StockDetail;
import cooba.IndustryPerformance.database.repository.BlackListReposiotry;
import cooba.IndustryPerformance.database.repository.IndustryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
public class IndustryService {
    @Autowired
    CrawlerService crawlerService;
    @Autowired
    IndustryRepository industryRepository;
    @Autowired
    StockService stockService;
    @Autowired
    BlackListReposiotry blackListReposiotry;
    @Autowired
    LocalcacheService localcacheService;

    public void biuldAllIndustryInfo() {
        UrlEnum[] urlEnums = UrlEnum.values();
        for (UrlEnum urlEnum : urlEnums) {
            asyncBuildIndustryInfo(urlEnum.name(), urlEnum.getUrl());
        }
    }

    @Async
    public void asyncBuildIndustryInfo(String industryType, String siteurl) {
        buildIndustryInfo(industryType, siteurl);
    }

    public void buildIndustryInfo(String industryType, String siteurl) {
        List<SubIndustry> subIndustryList = crawlerService.crawlIndustry(siteurl);
        Industry industry = Industry.builder()
                .industryName(industryType)
                .subIndustries(subIndustryList)
                .updatedTime(LocalDateTime.now())
                .build();

        if (!industryRepository.findByIndustryName(industryType).isPresent()) {
            industryRepository.save(industry);
            log.info("產業別:{}成功建立", industryType);
        } else {
            Industry oldindustry = industryRepository.findByIndustryName(industryType).get();
            log.info("產業別:{}已經存在 ,\n舊資料:{}\n新資料:{}", industryType, oldindustry, industry);
            industry.setId(oldindustry.getId());
            industryRepository.save(industry);
            log.info("產業別:{}成功更新", industryType);
        }
    }

    public void buildIndustryStockDetailInfo(String industryType) {
        Map<String, String> industryStockMap = getIndustryStockInfo(industryType);
        if (industryStockMap.isEmpty()) {
            log.warn("industryStockMap 為空");
            return;
        }
        List<CompletableFuture<StockDetail>> completableFutures = new ArrayList<>();
        industryStockMap.forEach((k, v) -> {
            completableFutures.add(CompletableFuture.supplyAsync(
                    () -> stockService.buildStockDetail(k), Executors.newFixedThreadPool(10)));
        });
        CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).join();
        log.info("buildIndustryStockDetailInfo 成功");
    }

    public Map<String, String> getIndustryStockInfo(String industryType) {
        Map<String, String> industryStockMap = new HashMap<>();
        if (industryRepository.findByIndustryName(industryType).isPresent()) {
            Industry industry = industryRepository.findByIndustryName(industryType).get();
            industry.getSubIndustries()
                    .forEach(subIndustry -> subIndustry.getCompanies()
                            .forEach(stock -> {
                                if (!localcacheService.getBlacklist().contains(stock.getStockcode())) {
                                    industryStockMap.put(stock.getStockcode(), stock.getName());
                                }
                            }));
        }
        return industryStockMap;
    }

    public BigDecimal getIndustryGrowth(String industryType) {
        AtomicReference<BigDecimal> lastprice = new AtomicReference<BigDecimal>(new BigDecimal(0));
        AtomicReference<BigDecimal> price = new AtomicReference<BigDecimal>(new BigDecimal(0));
        Map<String, String> industryStockMap = getIndustryStockInfo(industryType);
        if (industryStockMap.isEmpty()) {
            log.warn("industryStockMap 為空");
            return null;
        }
        for (Map.Entry<String, String> entry : industryStockMap.entrySet()) {
            String k = entry.getKey();
            String v = entry.getValue();
            StockDetail stock = stockService.getStockDetailLast_1_Day(k)
                    .orElseGet(() -> stockService.buildStockDetail(k));
            if (stock == null) {
                continue;
            }
            lastprice.updateAndGet(v1 -> v1.add(stock.getLastprice()));
            price.updateAndGet(v1 -> v1.add(stock.getPrice()));
        }
        BigDecimal result = new BigDecimal(0);
        BigDecimal growth = result.add(price.get()).subtract(lastprice.get()).divide(lastprice.get(), 4, RoundingMode.HALF_UP);
        log.info("產業:{} 漲幅:{} 今日股價:{} 昨日股價:{}", industryType, growth, price.get(), lastprice.get());
        return growth;
    }
}
