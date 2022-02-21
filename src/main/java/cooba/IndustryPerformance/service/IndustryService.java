package cooba.IndustryPerformance.service;

import cooba.IndustryPerformance.constant.UrlEnum;
import cooba.IndustryPerformance.database.entity.Industry.Industry;
import cooba.IndustryPerformance.database.entity.Industry.SubIndustry;
import cooba.IndustryPerformance.database.entity.StockDetail.StockDetail;
import cooba.IndustryPerformance.database.repository.IndustryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public void biuldAllIndustryInfo() {
        UrlEnum[] urlEnums = UrlEnum.values();
        for (UrlEnum urlEnum : urlEnums) {
            buildIndustryInfo(urlEnum.name(), urlEnum.getUrl());
        }
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
        industryStockMap.forEach((k, v) -> stockService.buildStockDetail(k));
        log.info("buildIndustryStockDetailInfo 成功");
    }

    public Map<String, String> getIndustryStockInfo(String industryType) {
        Map<String, String> industryStockMap = new HashMap<>();
        if (industryRepository.findByIndustryName(industryType).isPresent()) {
            Industry industry = industryRepository.findByIndustryName(industryType).get();
            industry.getSubIndustries()
                    .forEach(subIndustry -> subIndustry.getCompanies()
                            .forEach(stock -> industryStockMap.put(stock.getStockcode(), stock.getName())));
        }
        return industryStockMap;
    }

    public Float getIndustryGrowth(String industryType) {
        AtomicReference<Float> lastprice = new AtomicReference<>(Float.valueOf(0));
        AtomicReference<Float> price = new AtomicReference<>(Float.valueOf(0));
        Map<String, String> industryStockMap = getIndustryStockInfo(industryType);
        if (industryStockMap.isEmpty()) {
            log.warn("industryStockMap 為空");
            return null;
        }
        industryStockMap.forEach((k, v) -> {
            StockDetail stock = stockService.getStockDetailLast_1_Day(k)
                    .orElseGet(() -> stockService.buildStockDetail(k));
            lastprice.updateAndGet(v1 -> v1 + stock.getLastprice());
            price.updateAndGet(v1 -> v1 + stock.getPrice());
        });
        Float Growth = (price.get() - lastprice.get()) / lastprice.get();
        return Growth;
    }
}
