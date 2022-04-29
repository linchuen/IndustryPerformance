package cooba.IndustryPerformance.service;

import cooba.IndustryPerformance.constant.RedisConstant;
import cooba.IndustryPerformance.constant.StockConstant;
import cooba.IndustryPerformance.database.entity.Industry.Industry;
import cooba.IndustryPerformance.database.entity.Industry.Stock;
import cooba.IndustryPerformance.database.entity.Industry.SubIndustry;
import cooba.IndustryPerformance.database.entity.StockBasicInfo.StockBasicInfo;
import cooba.IndustryPerformance.database.entity.StockDetail.StockDetail;
import cooba.IndustryPerformance.database.repository.IndustryRepository;
import cooba.IndustryPerformance.database.repository.StockBasicInfoRepository;
import cooba.IndustryPerformance.enums.UrlEnum;
import cooba.IndustryPerformance.utility.RedisUtility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static cooba.IndustryPerformance.constant.StockConstant.LISTED;
import static cooba.IndustryPerformance.constant.StockConstant.OTC;

@Slf4j
@Service
public class IndustryService {
    @Autowired
    CrawlerService crawlerService;
    @Autowired
    IndustryRepository industryRepository;
    @Autowired
    StockBasicInfoRepository stockBasicInfoRepository;
    @Autowired
    StockService stockService;
    @Autowired
    RedisUtility redisUtility;

    private String today = LocalDate.now().toString();

    //@PostConstruct
    public void init() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("biuldAllIndustryInfo");
        log.info("Start biuldAllIndustryInfo");
        biuldAllIndustryInfo();
        stopWatch.stop();
        log.info("Task:{} 總夠耗時:{}s", stopWatch.getLastTaskName(), stopWatch.getLastTaskTimeMillis() / 1000);
        stopWatch.start("buildtodayStockDetail");
        log.info("Start buildtodayStockDetail");
        buildtodayStockDetail();
        stopWatch.stop();
        log.info("Task:{} 總夠耗時:{}s", stopWatch.getLastTaskName(), stopWatch.getLastTaskTimeMillis() / 1000);
    }

    public void biuldAllIndustryInfo() {
        UrlEnum[] urlEnums = UrlEnum.values();
        for (UrlEnum urlEnum : urlEnums) {
            asyncBuildIndustryInfo(urlEnum.name());
        }
    }

    public void buildtodayStockDetail() {
        UrlEnum[] urlEnums = UrlEnum.values();
        for (UrlEnum urlEnum : urlEnums) {
            buildIndustryStockDetailInfo(urlEnum.name());
            log.info("{}:{}", urlEnum.name(), getIndustryGrowth(urlEnum.name()));
        }
    }

    public void deleteAllIndustryInfo() {
        industryRepository.deleteAll();
    }

    @Async("industryExecutor")
    public void asyncBuildIndustryInfo(String industryType) {
        buildIndustryInfo(industryType);
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
                    stockService.asyncBuildStockBasicInfo(stock.getStockcode());
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

    public void buildIndustryStockDetailInfo(String industryType) {
        Map<String, String> industryStockMap = getIndustryStockInfo(industryType);
        if (industryStockMap.isEmpty()) {
            log.warn("industryStockMap 為空");
            return;
        }
        List<CompletableFuture<StockDetail>> completableFutures = new ArrayList<>();
        industryStockMap.forEach((k, v) -> {
            completableFutures.add(CompletableFuture.supplyAsync(
                    () -> {
                        StockDetail stockDetail = stockService.buildStockDetail(k);
                        return stockDetail;
                    }, Executors.newFixedThreadPool(3)));
        });
        CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).join();
        log.info("buildIndustryStockDetailInfo 成功");
    }

    /*
     * Get Method
     * */

    public List<Industry> getAllIndustry() {
        List<Industry> industrylist = new ArrayList<>();
        for (UrlEnum urlEnum : UrlEnum.values()) {
            String industryType = urlEnum.name();
            String key = RedisConstant.INDUSTRY + industryType;

            if (redisUtility.hasKey(key)) {
                log.info("取得 Industry:{} redis資訊 ", industryType);
                Industry industry = (Industry) redisUtility.valueObjectGet(key, Industry.class);
                industrylist.add(industry);
            } else {
                synchronized (LocalcacheService.getIndustryLock(industryType)) {
                    if (redisUtility.hasKey(key)) {
                        log.info("取得 Industry: {} redis synchronized資訊", industryType);
                        Industry industry = (Industry) redisUtility.valueObjectGet(key, Industry.class);
                        industrylist.add(industry);
                    } else {
                        industryRepository.findByIndustryName(industryType).ifPresent(industry -> {
                            redisUtility.valueObjectSet(key, industry);
                            industrylist.add(industry);
                        });
                    }
                }
            }
        }
        return industrylist;
    }

    public Map<String, String> getIndustryStockInfo(String industryType) {
        String key = RedisConstant.INDUSTRYINFO + industryType;

        if (redisUtility.hasKey(key)) {
            log.info("取得 IndustryStockInfo: {} redis資訊", industryType);
            return redisUtility.Map(key).entries();
        } else {
            synchronized (LocalcacheService.getIndustryLock(industryType)) {
                if (redisUtility.hasKey(key)) {
                    log.info("取得 IndustryStockInfo: {} redis synchronized資訊", industryType);
                    return redisUtility.Map(key).entries();
                }
                Map<String, String> industryStockMap = new HashMap<>();
                industryRepository.findByIndustryName(industryType).ifPresent(industry -> {
                    log.info("取得 IndustryStockInfo: {} mongodb資訊", industryType);
                    industry.getSubIndustries()
                            .forEach(subIndustry -> subIndustry.getCompanies()
                                    .forEach(stock -> {
                                        if (!redisUtility.hasKey(RedisConstant.BLACKLIST + stock.getStockcode())) {
                                            industryStockMap.put(stock.getStockcode(), stock.getName());
                                            redisUtility.mapPut(key, stock.getStockcode(), stock.getName());
                                        }
                                    }));
                });
                return industryStockMap;
            }
        }
    }

    public Set<String> getSubIndustryInfo(String industryType) {
        String key = RedisConstant.INDUSTRYINFO + industryType + ":subIndustry";

        if (redisUtility.hasKey(key)) {
            log.info("取得 SubIndustryInfo: {} redis資訊", industryType);
            return redisUtility.Set(key).members();
        } else {
            synchronized (LocalcacheService.getIndustryLock(industryType)) {
                if (redisUtility.hasKey(key)) {
                    log.info("取得 SubIndustryInfo: {} redis synchronized資訊", industryType);
                    return redisUtility.Set(key).members();
                }
                Set<String> subIndustrySet = new HashSet<>();
                if (industryRepository.findByIndustryName(industryType).isPresent()) {
                    Industry industry = industryRepository.findByIndustryName(industryType).get();
                    log.info("取得 SubIndustryInfo: {} mongodb資訊", industryType);
                    industry.getSubIndustries()
                            .forEach(subIndustry -> {
                                subIndustrySet.add(subIndustry.getSubIndustryName());
                                redisUtility.setAdd(key, subIndustry.getSubIndustryName());
                            });
                    return subIndustrySet;
                }
                return subIndustrySet;
            }
        }
    }

    public Map<String, String> getSubIndustryStockInfo(String industryType, String subIndustryName) {
        String key = RedisConstant.INDUSTRYINFO + industryType + ":" + subIndustryName;

        if (redisUtility.hasKey(key)) {
            log.info("取得SubIndustryStockInfo: {} {} redis資訊", industryType, subIndustryName);
            return redisUtility.Map(key).entries();
        } else {
            synchronized (LocalcacheService.getSubIndustryLock(subIndustryName)) {
                if (redisUtility.hasKey(key)) {
                    log.info("取得SubIndustryStockInfo: {} {} redis synchronized資訊", industryType, subIndustryName);
                    return redisUtility.Map(key).entries();
                }
                Map<String, String> subIndustryMap = new HashMap<>();
                if (industryRepository.findByIndustryName(industryType).isPresent()) {
                    Industry industry = industryRepository.findByIndustryName(industryType).get();
                    log.info("取得SubIndustryStockInfo: {} {} mongodb資訊", industryType, subIndustryName);
                    for (SubIndustry subIndustry : industry.getSubIndustries()) {
                        if (subIndustryName.equals(subIndustry.getSubIndustryName())) {
                            BoundHashOperations boundHashOperations = redisUtility.Map(key);
                            boundHashOperations.putAll(subIndustry.getCompanies().stream().collect(Collectors.toMap(Stock::getStockcode, Stock::getName)));
                            subIndustryMap = subIndustry.getCompanies().stream().collect(Collectors.toMap(Stock::getStockcode, Stock::getName));
                        }
                    }
                    return subIndustryMap;
                }
                return subIndustryMap;
            }
        }
    }

    public BigDecimal getGrowth(int days, Map<String, String> StockMap) {
        List<String> stocklist = new ArrayList<>();
        if (StockMap.isEmpty()) {
            log.warn("StockMap 為空");
            return null;
        }
        AtomicReference<BigDecimal> price = new AtomicReference<BigDecimal>(new BigDecimal(0));
        AtomicReference<BigDecimal> last_n_daysPrice = new AtomicReference<BigDecimal>(new BigDecimal(0));

        for (Entry<String, String> entry : StockMap.entrySet()) {
            String stockcode = entry.getKey();
            String stockname = entry.getValue();

            StockDetail stock = stockService.getStockDetailToday(stockcode)
                    .orElseGet(() -> stockService.buildStockDetail(stockcode));
            if (days > 1) {
                StockDetail stock_n = stockService.getStockDetailLast_n_day(stockcode, days).orElse(null);
                if (stock == null || stock_n == null) {
                    log.warn("{} {}找不到資料", stockcode, stockname);
                    continue;
                }
                stocklist.add(stock.getName());
                price.updateAndGet(v1 -> v1.add(stock.getPrice()));
                last_n_daysPrice.updateAndGet(v1 -> v1.add(stock_n.getPrice()));
            } else {
                if (stock == null) {
                    log.warn("{} {}找不到資料", stockcode, stockname);
                    continue;
                }
                stocklist.add(stock.getName());
                price.updateAndGet(v1 -> v1.add(stock.getPrice()));
                last_n_daysPrice.updateAndGet(v1 -> v1.add(stock.getLastprice()));
            }
        }
        BigDecimal result = new BigDecimal(0);
        BigDecimal growth;
        try {
            growth = result.add(price.get()).subtract(last_n_daysPrice.get()).divide(last_n_daysPrice.get(), 4, RoundingMode.HALF_UP);
        } catch (ArithmeticException exception) {
            growth = new BigDecimal(0);
        }
        return growth;
    }

    public BigDecimal getIndustryGrowth(String industryType) {
        return getIndustry_n_DaysGrowth(1, industryType, StockConstant.LISTEDOTC);
    }

    public BigDecimal getIndustryGrowth(String industryType, String companyType) {
        return getIndustry_n_DaysGrowth(1, industryType, companyType);
    }

    public BigDecimal getIndustry_n_DaysGrowth(int days, String industryType, String companyType) {
        String key = RedisConstant.GROWTH + industryType + ":" + companyType + ":" + today;
        key = days > 1 ? key + ":" + days : key;
        if (redisUtility.hasKey(key)) {
            log.info("取得Growth: {} redis資訊", industryType);
            BigDecimal growth = new BigDecimal(redisUtility.valueGet(key));
            return growth;
        }
        Map<String, String> stockMap = getIndustryStockInfo(industryType);
        List<String> stocklist;
        Map<String, String> newstockMap;
        BigDecimal growth;
        switch (companyType) {
            case LISTED:
                stocklist = stockBasicInfoRepository.findByCompanyType(LISTED)
                        .stream()
                        .map(StockBasicInfo::getStockcode)
                        .collect(Collectors.toList());
                newstockMap = stockMap.entrySet().stream().filter(entry -> stocklist.contains(entry.getKey())).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
                growth = getGrowth(days, newstockMap);
                redisUtility.valueSet(key, String.valueOf(growth));
                log.info("{}產業:{} {}日漲幅:{} 列表:{}", companyType, industryType, days, growth, stocklist);
                break;
            case OTC:
                stocklist = stockBasicInfoRepository.findByCompanyType(OTC)
                        .stream()
                        .map(StockBasicInfo::getStockcode)
                        .collect(Collectors.toList());
                newstockMap = stockMap.entrySet().stream().filter(entry -> stocklist.contains(entry.getKey())).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
                growth = getGrowth(days, newstockMap);
                redisUtility.valueSet(key, String.valueOf(growth));
                log.info("{}產業:{} {}日漲幅:{} 列表:{}", companyType, industryType, days, growth, stocklist);
                break;
            default:
                growth = getGrowth(days, stockMap);
                redisUtility.valueSet(key, String.valueOf(growth));
                log.info("{}產業:{} {}日漲幅:{} 列表:{}", companyType, industryType, days, growth, stockMap);
                break;
        }
        return growth;
    }

    public BigDecimal getSubIndustryGrowth(String industryType, String subIndustryName) {
        return getSubIndustry_n_DaysGrowth(1, industryType, subIndustryName, StockConstant.LISTEDOTC);
    }

    public BigDecimal getSubIndustryGrowth(String industryType, String subIndustryName, String companyType) {
        return getSubIndustry_n_DaysGrowth(1, industryType, subIndustryName, companyType);
    }

    public BigDecimal getSubIndustry_n_DaysGrowth(int days, String industryType, String subIndustryName, String companyType) {
        String key = RedisConstant.GROWTH + industryType + ":" + subIndustryName + ":" + companyType + ":" + today;
        key = days > 1 ? key + ":" + days : key;
        if (redisUtility.hasKey(key)) {
            log.info("取得Growth: {} redis資訊", industryType);
            BigDecimal growth = new BigDecimal(redisUtility.valueGet(key));
            return growth;
        }
        Map<String, String> stockMap = getSubIndustryStockInfo(industryType, subIndustryName);
        List<String> stocklist;
        Map<String, String> newstockMap;
        BigDecimal growth;
        switch (companyType) {
            case LISTED:
                stocklist = stockBasicInfoRepository.findByCompanyType(LISTED)
                        .stream()
                        .map(StockBasicInfo::getStockcode)
                        .collect(Collectors.toList());
                newstockMap = stockMap.entrySet().stream().filter(entry -> stocklist.contains(entry.getKey())).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
                growth = getGrowth(days, newstockMap);
                redisUtility.valueSet(key, String.valueOf(growth));
                log.info("{}產業:{} {}日漲幅:{} 列表:{}", companyType, subIndustryName, days, growth, stocklist);
                break;
            case OTC:
                stocklist = stockBasicInfoRepository.findByCompanyType(OTC)
                        .stream()
                        .map(StockBasicInfo::getStockcode)
                        .collect(Collectors.toList());
                newstockMap = stockMap.entrySet().stream().filter(entry -> stocklist.contains(entry.getKey())).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
                growth = getGrowth(days, newstockMap);
                redisUtility.valueSet(key, String.valueOf(growth));
                log.info("{}產業:{} {}日漲幅:{} 列表:{}", companyType, subIndustryName, days, growth, stocklist);
                break;
            default:
                growth = getGrowth(days, stockMap);
                redisUtility.valueSet(key, String.valueOf(growth));
                log.info("{}產業:{} {}日漲幅:{} 列表:{}", companyType, subIndustryName, days, growth, stockMap);
                break;
        }
        return growth;
    }
}