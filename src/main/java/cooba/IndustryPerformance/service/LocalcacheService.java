package cooba.IndustryPerformance.service;

import cooba.IndustryPerformance.constant.RedisConstant;
import cooba.IndustryPerformance.constant.UrlEnum;
import cooba.IndustryPerformance.database.entity.Industry.Industry;
import cooba.IndustryPerformance.database.entity.Industry.SubIndustry;
import cooba.IndustryPerformance.database.repository.IndustryRepository;
import cooba.IndustryPerformance.database.repository.StockDetailRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LocalcacheService {
    @Autowired
    StockDetailRepository stockDetailRepository;
    @Autowired
    IndustryRepository industryRepository;
    @Autowired
    CrawlerService crawlerService;
    @Autowired
    RedisTemplate redisTemplate;

    public static List<String> industryLock = new ArrayList<>();
    public static List<String> subindustryLock = new ArrayList<>();
    public static List<String> stockcodeLock = new ArrayList<>();

    @PostConstruct
    public void init() {
        log.info("init");
        stockDetailRepository.findByCompanyType("興櫃")
                .forEach(stockDetail -> redisTemplate.opsForValue().set(RedisConstant.BLACKLIST + stockDetail.getStockcode(), stockDetail.getStockcode()));
        industryLock = Arrays.stream(UrlEnum.values()).map(o -> o.name()).collect(Collectors.toList());
        for (UrlEnum urlEnum : UrlEnum.values()) {
            if (!industryRepository.findByIndustryName(urlEnum.name()).isPresent()) {
                List<SubIndustry> subIndustryList = crawlerService.crawlIndustry(urlEnum.getUrl());
                Industry industry = Industry.builder()
                        .industryName(urlEnum.name())
                        .subIndustries(subIndustryList)
                        .updatedTime(LocalDateTime.now())
                        .build();
                industryRepository.save(industry);
                industry.getSubIndustries().forEach(subIndustry -> {
                    subindustryLock.add(subIndustry.getSubIndustryName());
                    subIndustry.getCompanies().forEach(stock -> stockcodeLock.add(stock.getStockcode()));
                });
            }else{
                Industry industry = industryRepository.findByIndustryName(urlEnum.name()).get();
                industry.getSubIndustries().forEach(subIndustry -> {
                    subindustryLock.add(subIndustry.getSubIndustryName());
                    subIndustry.getCompanies().forEach(stock -> stockcodeLock.add(stock.getStockcode()));
                });
            }
        }
    }

    public String getIndustryLock(String industryType) {
        for (String s : industryLock) {
            if (s.equals(industryType)) return s;
        }
        return "";
    }

    public String getSubIndustryLock(String subIndustryName) {
        for (String s : industryLock) {
            if (s.equals(subIndustryName)) return s;
        }
        return "";
    }

    public String getStockcodeLock(String stockcode) {
        for (String s : stockcodeLock) {
            if (s.equals(stockcode)) return s;
        }
        return "";
    }

    public List<String> updateStockcodeLockList(List<String> oldStockcodeLockList,List<String> newStockcodeLockList) {
        oldStockcodeLockList.clear();
        oldStockcodeLockList.addAll(newStockcodeLockList);
        return oldStockcodeLockList;
    }

    public List<String> getStockcodeLockList() {
        return stockcodeLock;
    }
}
