package cooba.IndustryPerformance.service;

import cooba.IndustryPerformance.constant.RedisConstant;
import cooba.IndustryPerformance.constant.UrlEnum;
import cooba.IndustryPerformance.database.entity.Industry.Industry;
import cooba.IndustryPerformance.database.entity.Industry.Stock;
import cooba.IndustryPerformance.database.entity.Industry.SubIndustry;
import cooba.IndustryPerformance.database.entity.StockDetail.StockDetail;
import cooba.IndustryPerformance.database.repository.IndustryRepository;
import cooba.IndustryPerformance.database.repository.StockDetailRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

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
    StockDetailRepository stockDetailRepository;
    @Autowired
    LocalcacheService localcacheService;
    @Autowired
    RedisTemplate redisTemplate;

    public void biuldAllIndustryInfo() {
        UrlEnum[] urlEnums = UrlEnum.values();
        for (UrlEnum urlEnum : urlEnums) {
            asyncBuildIndustryInfo(urlEnum.name());
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
                    BoundSetOperations subIndustrySetOperations = redisTemplate.boundSetOps(RedisConstant.INDUSTRYINFO + industryType + ":subIndustry");
                    subIndustrySetOperations.add(subIndustry.getSubIndustryName());
                    BoundHashOperations<String, String, Object> subIndustryMapOperations = redisTemplate.boundHashOps(RedisConstant.INDUSTRYINFO + industryType + ":" + subIndustry.getSubIndustryName());
                    subIndustryMapOperations.put(stock.getStockcode(), stock.getName());
                    BoundHashOperations<String, String, Object> boundHashOperations = redisTemplate.boundHashOps(RedisConstant.INDUSTRYINFO + industryType);
                    boundHashOperations.put(stock.getStockcode(), stock.getName());
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
                    () -> stockService.buildStockDetail(k), Executors.newFixedThreadPool(5)));
        });
        CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).join();
        log.info("buildIndustryStockDetailInfo 成功");
    }

    public Map<String, String> getIndustryStockInfo(String industryType) {
        if (redisTemplate.hasKey(RedisConstant.INDUSTRYINFO + industryType)) {
            Map<String, String> industryStockMap = new HashMap<>();
            redisTemplate.boundHashOps(RedisConstant.INDUSTRYINFO + industryType).entries();

            log.info("已從redis取得產業 {} 資訊", industryType);
            return industryStockMap;
        } else {
            synchronized (localcacheService.getIndustryLock(industryType)) {
                if (redisTemplate.hasKey(RedisConstant.INDUSTRYINFO + industryType)) {
                    log.info("已從mongo新增redis並取得產業 {} 資訊", industryType);
                    return redisTemplate.boundHashOps(RedisConstant.INDUSTRYINFO + industryType).entries();
                }
                Map<String, String> industryStockMap = new HashMap<>();
                if (industryRepository.findByIndustryName(industryType).isPresent()) {
                    Industry industry = industryRepository.findByIndustryName(industryType).get();
                    log.info("已從mongo取得產業 {} 資訊", industryType);
                    industry.getSubIndustries()
                            .forEach(subIndustry -> subIndustry.getCompanies()
                                    .forEach(stock -> {
                                        if (!redisTemplate.hasKey(RedisConstant.BLACKLIST + stock.getStockcode())) {
                                            industryStockMap.put(stock.getStockcode(), stock.getName());
                                            redisTemplate.boundHashOps(RedisConstant.INDUSTRYINFO + industryType).put(stock.getStockcode(), stock.getName());
                                        }
                                    }));
                    return industryStockMap;
                }
                return industryStockMap;
            }
        }
    }

    public Set<String> getSubIndustryInfo(String industryType) {
        if (redisTemplate.hasKey(RedisConstant.INDUSTRYINFO + industryType + ":subIndustry")) {
            log.info("已從redis取得副產業 {} 類別資訊", industryType);
            return redisTemplate.boundSetOps(RedisConstant.INDUSTRYINFO + industryType + ":subIndustry").members();
        } else {
            synchronized (localcacheService.getSubIndustryLock(industryType)) {
                if (redisTemplate.hasKey(RedisConstant.INDUSTRYINFO + industryType + ":subIndustry")) {
                    log.info("已從mongo新增redis並取得副產業 {} 類別資訊", industryType);
                    return redisTemplate.boundSetOps(RedisConstant.INDUSTRYINFO + industryType + ":subIndustry").members();
                }
                Set<String> subIndustrySet = new HashSet<>();
                if (industryRepository.findByIndustryName(industryType).isPresent()) {
                    Industry industry = industryRepository.findByIndustryName(industryType).get();
                    log.info("已從mongo取得副產業 {} 類別資訊", industryType);
                    industry.getSubIndustries()
                            .forEach(subIndustry -> {
                                subIndustrySet.add(subIndustry.getSubIndustryName());
                                redisTemplate.boundSetOps(RedisConstant.INDUSTRYINFO + industryType + ":subIndustry").add(subIndustry.getSubIndustryName());
                            });
                    return subIndustrySet;
                }
                return subIndustrySet;
            }
        }
    }

    public Map<String, String> getSubIndustryStockInfo(String industryType, String subIndustryName) {
        if (redisTemplate.hasKey(RedisConstant.INDUSTRYINFO + industryType + ":" + subIndustryName)) {
            log.info("已從redis取得副產業 {} {} 資訊", industryType, subIndustryName);
            return redisTemplate.boundHashOps(RedisConstant.INDUSTRYINFO + industryType + ":" + subIndustryName).entries();
        } else {
            synchronized (localcacheService.getSubIndustryLock(industryType)) {
                if (redisTemplate.hasKey(RedisConstant.INDUSTRYINFO + industryType + ":" + subIndustryName)) {
                    log.info("已從mongo新增redis並取得副產業 {} {} 資訊", industryType, subIndustryName);
                    return redisTemplate.boundHashOps(RedisConstant.INDUSTRYINFO + industryType + ":" + subIndustryName).entries();
                }
                Map<String, String> subIndustryMap = new HashMap<>();
                if (industryRepository.findByIndustryName(industryType).isPresent()) {
                    Industry industry = industryRepository.findByIndustryName(industryType).get();
                    log.info("已從mongo取得副產業 {} {} 資訊", industryType, subIndustryName);
                    for (SubIndustry subIndustry : industry.getSubIndustries()) {
                        if (subIndustryName.equals(subIndustry.getSubIndustryName())) {
                            BoundHashOperations boundHashOperations = redisTemplate.boundHashOps(RedisConstant.INDUSTRYINFO + industryType + ":" + subIndustryName);
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

    public BigDecimal getGrowth(String industryType,int days, Map<String, String> StockMap) {
        List<String> stocklist=new ArrayList<>();
        if (StockMap.isEmpty()) {
            log.warn("StockMap 為空");
            return null;
        }
        AtomicReference<BigDecimal> price = new AtomicReference<BigDecimal>(new BigDecimal(0));
        AtomicReference<BigDecimal> last_n_daysPrice = new AtomicReference<BigDecimal>(new BigDecimal(0));

        for (Entry<String, String> entry : StockMap.entrySet()) {
            String k = entry.getKey();
            String v = entry.getValue();

            StockDetail stock = stockService.getStockDetailToday(k).orElseGet(() -> stockService.buildStockDetail(k));
            if (days > 1) {
                StockDetail stock_n = stockService.getStockDetailLast_n_day(k, days).orElseGet(null);
                if (stock == null || stock_n == null) {
                    log.warn("{} {}找不到資料", k, v);
                    continue;
                }
                stocklist.add(stock.getName());
                price.updateAndGet(v1 -> v1.add(stock.getPrice()));
                last_n_daysPrice.updateAndGet(v1 -> v1.add(stock_n.getPrice()));
            } else {
                if (stock == null) {
                    log.warn("{} {}找不到資料", k, v);
                    continue;
                }
                stocklist.add(stock.getName());
                price.updateAndGet(v1 -> v1.add(stock.getPrice()));
                last_n_daysPrice.updateAndGet(v1 -> v1.add(stock.getLastprice()));
            }
        }
        BigDecimal result = new BigDecimal(0);
        BigDecimal growth = result.add(price.get()).subtract(last_n_daysPrice.get()).divide(last_n_daysPrice.get(), 4, RoundingMode.HALF_UP);
        log.info("產業:{} 漲幅:{} 今日股價和:{} {}日股價和:{} 列表:{}",industryType, growth, price.get(), days, last_n_daysPrice.get(),stocklist);
        return growth;
    }

    public BigDecimal getIndustryGrowth(String industryType) {
        return getIndustry_n_DaysGrowth(1, industryType);
    }

    public BigDecimal getIndustry_n_DaysGrowth(int days, String industryType) {
        BigDecimal growth = getGrowth(industryType,days, getIndustryStockInfo(industryType));
        return growth;
    }

    public BigDecimal getSubIndustryGrowth(String industryType, String subIndustryName) {
        return getSubIndustry_n_DaysGrowth(1, industryType, subIndustryName);
    }

    public BigDecimal getSubIndustry_n_DaysGrowth(int days, String industryType, String subIndustryName) {
        BigDecimal growth = getGrowth(subIndustryName,days, getSubIndustryStockInfo(industryType, subIndustryName));
        return growth;
    }
}