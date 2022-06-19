package cooba.IndustryPerformance.service;

import cooba.IndustryPerformance.database.entity.Industry.Industry;
import cooba.IndustryPerformance.database.entity.Industry.SubIndustry;
import cooba.IndustryPerformance.database.entity.SkipDate.SkipDate;
import cooba.IndustryPerformance.database.entity.StockBasicInfo.StockBasicInfo;
import cooba.IndustryPerformance.database.repository.IndustryRepository;
import cooba.IndustryPerformance.database.repository.SkipDateRepository;
import cooba.IndustryPerformance.database.repository.StockBasicInfoRepository;
import cooba.IndustryPerformance.enums.UrlEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static cooba.IndustryPerformance.constant.StockConstant.LISTED;

@Slf4j
@Service
public class LocalcacheService {
    @Autowired
    IndustryRepository industryRepository;
    @Autowired
    StockBasicInfoRepository stockBasicInfoRepository;
    @Autowired
    SkipDateRepository skipDateRepository;
    @Autowired
    CrawlerService crawlerService;

    public static List<String> industryLock = new ArrayList<>();
    public static List<String> subindustryLock = new ArrayList<>();
    public static Set<String> stockcodeLock = new HashSet<>();
    public static List<LocalDate> skipDateList = new ArrayList<>();
    public static Set<String> listedStockList = new HashSet<>();
    public static Set<String> listedStockTimeToMarketLessThan1YearList = new HashSet<>();
    ;

    @PostConstruct
    public void init() {
        log.info("init");
        industryLock = Arrays.stream(UrlEnum.values()).map(o -> o.name()).collect(Collectors.toList());
        updateStockcodeLockMap();
        skipDateList = skipDateRepository.findAll().stream().map(SkipDate::getSkipDate).collect(Collectors.toList());
        listedStockList = stockBasicInfoRepository.findByCompanyType(LISTED)
                .stream()
                .map(StockBasicInfo::getStockcode)
                .collect(Collectors.toSet());
        listedStockTimeToMarketLessThan1YearList = stockBasicInfoRepository.findByTimeToMarketGreaterThan(LocalDate.now().minusYears(1))
                .stream()
                .filter(stockBasicInfo -> stockBasicInfo.getCompanyType().equals(LISTED))
                .map(StockBasicInfo::getStockcode)
                .collect(Collectors.toSet());
    }

    public static String getIndustryLock(String industryType) {
        for (String s : industryLock) {
            if (s.equals(industryType)) return s;
        }
        return "";
    }

    public static String getSubIndustryLock(String subIndustryName) {
        for (String s : industryLock) {
            if (s.equals(subIndustryName)) return s;
        }
        return "";
    }

    public static String getStockcodeLock(String stockcode) {
        for (String s : stockcodeLock) {
            if (s.equals(stockcode)) return s;
        }
        return "";
    }

    public static List<LocalDate> getSkipDateList() {
        return skipDateList;
    }

    public static Set<String> getListedStockList() {
        return listedStockList;
    }

    public static Set<String> getlistedStockTimeToMarketLessThan1YearList() {
        return listedStockTimeToMarketLessThan1YearList;
    }

    public void updateStockcodeLockMap() {
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
                    subIndustry.getCompanies().forEach(stock -> stockcodeLock.add(stock.getStockcode()));
                });
            } else {
                Industry industry = industryRepository.findByIndustryName(urlEnum.name()).get();
                industry.getSubIndustries().forEach(subIndustry -> {
                    subIndustry.getCompanies().forEach(stock -> stockcodeLock.add(stock.getStockcode()));
                });
            }
        }
    }
}
