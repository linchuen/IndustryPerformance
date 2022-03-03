package cooba.IndustryPerformance.service;

import cooba.IndustryPerformance.constant.RedisConstant;
import cooba.IndustryPerformance.constant.UrlEnum;
import cooba.IndustryPerformance.database.entity.Industry.Industry;
import cooba.IndustryPerformance.database.entity.Industry.Stock;
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
    public static Map<String,List<String>> stockcodeLock = new HashMap<String, List<String>>();

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
                    stockcodeLock.put(urlEnum.name(),subIndustry.getCompanies().stream().map(stock->stock.getStockcode()).collect(Collectors.toList()));
                });
            }else{
                Industry industry = industryRepository.findByIndustryName(urlEnum.name()).get();
                industry.getSubIndustries().forEach(subIndustry -> {
                    subindustryLock.add(subIndustry.getSubIndustryName());
                    stockcodeLock.put(urlEnum.name(),subIndustry.getCompanies().stream().map(stock->stock.getStockcode()).collect(Collectors.toList()));
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
        for (Map.Entry Lockindustry : stockcodeLock.entrySet()) {
            for(String lockStcokCode:(List<String>)Lockindustry.getValue()){
                if(lockStcokCode.equals(stockcode)){
                    return lockStcokCode;
                }
            }
        }
        return "";
    }

    public Map<String,List<String>> updateStockcodeLockMap(String industryType,Map<String,List<String>> oldStockcodeLockMap,List<String> newStockcodeLockList) {
        oldStockcodeLockMap.computeIfPresent(industryType,(k,v)->v=newStockcodeLockList);
        return oldStockcodeLockMap;
    }

    public Map<String,List<String>> getStockcodeLockList() {
        return stockcodeLock;
    }
}
