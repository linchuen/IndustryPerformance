package cooba.IndustryPerformance.service;

import cooba.IndustryPerformance.constant.UrlEnum;
import cooba.IndustryPerformance.database.entity.Industry.Industry;
import cooba.IndustryPerformance.database.entity.Industry.Stock;
import cooba.IndustryPerformance.database.entity.Industry.SubIndustry;
import cooba.IndustryPerformance.database.repository.IndustryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            log.info("{}成功存儲", industryType);
        } else {
            Industry oldindustry = industryRepository.findByIndustryName(industryType).get();
            log.info("{}已經存在 ,{}", industryType, oldindustry);
        }
    }

    public void buildIndustryStockDetailInfo(String industryType) {
        Map<String, String> industryStockMap = getIndustryStockInfo(industryType);
        industryStockMap.forEach((k, v) -> {
            try {
                stockService.buildStockDetail(k);
            } catch (Exception e) {
                log.error("{} {} 發生錯誤，無法建立股票資訊", k, v);
            }
        });
    }

    public Map<String, String> getIndustryStockInfo(String industryType) {
        Map<String, String> industryStockMap = new HashMap<>();
        if (industryRepository.findByIndustryName(industryType).isPresent()) {
            Industry industry = industryRepository.findByIndustryName(industryType).get();
            for (SubIndustry subIndustry : industry.getSubIndustries()) {
                for (Stock stock : subIndustry.getCompanies()) {
                    industryStockMap.put(stock.getStockcode(), stock.getName());
                }
            }
        }
        log.info(industryStockMap.toString());
        return industryStockMap;
    }
}
