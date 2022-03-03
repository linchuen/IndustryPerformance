package cooba.IndustryPerformance.service;

import cooba.IndustryPerformance.constant.RedisConstant;
import cooba.IndustryPerformance.constant.UrlEnum;
import cooba.IndustryPerformance.database.repository.IndustryRepository;
import cooba.IndustryPerformance.database.repository.StockDetailRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
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
    IndustryService industryService;
    @Autowired
    RedisTemplate redisTemplate;

    public static List<String> industryLock = new ArrayList<>();
    public static List<String> subindustryLock = new ArrayList<>();
    public static List<String> stockcodeLock=new ArrayList<>();

    @PostConstruct
    public void init() {
        log.info("init");
        stockDetailRepository.findByCompanyType("興櫃")
                .forEach(stockDetail -> redisTemplate.opsForValue().set(RedisConstant.BLACKLIST + stockDetail.getStockcode(), stockDetail.getStockcode()));
        industryLock = Arrays.stream(UrlEnum.values()).map(o -> o.name()).collect(Collectors.toList());
        for (UrlEnum urlEnum:UrlEnum.values()) {
            if (!industryRepository.findByIndustryName(urlEnum.name()).isPresent()){
                industryService.buildIndustryInfo(urlEnum.name());
            }
            for (String s:industryService.getSubIndustryInfo(urlEnum.name())){
                subindustryLock.add(s);
            }
            industryService.getIndustryStockInfo(urlEnum.name()).entrySet().forEach(entry->stockcodeLock.add(entry.getKey()));
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
}
