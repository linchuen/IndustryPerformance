package cooba.IndustryPerformance.service;

import cooba.IndustryPerformance.constant.RedisConstant;
import cooba.IndustryPerformance.database.entity.Industry.Industry;
import cooba.IndustryPerformance.database.entity.Industry.SubIndustry;
import cooba.IndustryPerformance.database.entity.StockDetail.StockDetail;
import cooba.IndustryPerformance.database.repository.IndustryRepository;
import cooba.IndustryPerformance.database.repository.StockDetailRepository;
import cooba.IndustryPerformance.enums.UrlEnum;
import cooba.IndustryPerformance.utility.RedisUtility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
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
    RedisUtility redisUtility;

    public static List<String> industryLock = new ArrayList<>();
    public static List<String> subindustryLock = new ArrayList<>();
    public static Set<String> stockcodeLock = new HashSet<>();

    @PostConstruct
    public void init() {
        log.info("init");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("biuldAllIndustryInfo");
        log.info("Start biuldAllIndustryInfo");
        //biuldAllIndustryInfo();
        stopWatch.stop();
        log.info("Task:{} 總夠耗時:{}", stopWatch.getLastTaskName(), stopWatch.getLastTaskTimeMillis());
        stopWatch.start("buildtodayStockDetail");
        log.info("Start buildtodayStockDetail");
        buildtodayStockDetail();
        stopWatch.stop();
        log.info("Task:{} 總夠耗時:{}", stopWatch.getLastTaskName(), stopWatch.getLastTaskTimeMillis());
        stockDetailRepository.findByCompanyType("興櫃")
                .forEach(stockDetail -> redisUtility.valueSet(RedisConstant.BLACKLIST + stockDetail.getStockcode(), stockDetail.getStockcode()));
        industryLock = Arrays.stream(UrlEnum.values()).map(o -> o.name()).collect(Collectors.toList());
        updateStockcodeLockMap();
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

    public void buildtodayStockDetail() {
        industryRepository.findAll().forEach(industry ->
                industry.getSubIndustries().forEach(subIndustry ->
                        subIndustry.getCompanies().forEach(stock -> {
                                    if (!stockDetailRepository.findByStockcodeAndCreatedTime(stock.getStockcode(), LocalDate.now()).isPresent()) {
                                        StockDetail stockDetail = crawlerService.crawlSecondarySourceStock(stock.getStockcode());
                                        if (stockDetail != null) {
                                            stockDetailRepository.save(stockDetail);
                                        }

                                    }
                                }
                        )
                )
        );
    }

    /*
     * 跟IndustryService一樣
     *
     * */
    public void biuldAllIndustryInfo() {
        UrlEnum[] urlEnums = UrlEnum.values();
        for (UrlEnum urlEnum : urlEnums) {
            buildIndustryInfo(urlEnum.name());
        }
    }

    public void buildIndustryInfo(String industryType) {
        String siteurl = UrlEnum.valueOf(industryType).getUrl();
        List<SubIndustry> subIndustryList = crawlerService.crawlIndustry(siteurl);
        Industry industry = Industry.builder()
                .industryName(industryType)
                .subIndustries(subIndustryList)
                .updatedTime(LocalDateTime.now())
                .build();

        subIndustryList.forEach(subIndustry -> subIndustry.getCompanies()
                .forEach(stock -> {
                    redisUtility.setAdd(RedisConstant.INDUSTRYINFO + industryType + ":subIndustry", subIndustry.getSubIndustryName());
                    redisUtility.mapPut(RedisConstant.INDUSTRYINFO + industryType + ":" + subIndustry.getSubIndustryName(), stock.getStockcode(), stock.getName());
                    redisUtility.mapPut(RedisConstant.INDUSTRYINFO + industryType, stock.getStockcode(), stock.getName());
                }));

        if (!industryRepository.findByIndustryName(industryType).isPresent()) {
            industryRepository.save(industry);
            log.info("產業別:{}成功建立", industryType);
        } else {
            Industry oldindustry = industryRepository.findByIndustryName(industryType).get();
            log.info("產業別:{}已經存在 ", industryType);
            industry.setId(oldindustry.getId());
            industryRepository.save(industry);
            log.info("產業別:{}成功更新", industryType);
        }
    }
}
