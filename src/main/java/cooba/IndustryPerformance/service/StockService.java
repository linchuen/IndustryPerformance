package cooba.IndustryPerformance.service;

import cooba.IndustryPerformance.constant.RedisConstant;
import cooba.IndustryPerformance.database.entity.StockBasicInfo.StockBasicInfo;
import cooba.IndustryPerformance.database.entity.StockDetail.StockDetail;
import cooba.IndustryPerformance.database.mapper.StockDetailMapper;
import cooba.IndustryPerformance.database.repository.StockBasicInfoRepository;
import cooba.IndustryPerformance.database.repository.StockDetailRepository;
import cooba.IndustryPerformance.utility.RedisUtility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class StockService {
    @Autowired
    StockDetailRepository stockDetailRepository;
    @Autowired
    StockDetailMapper stockDetailMapper;
    @Autowired
    StockBasicInfoRepository stockBasicInfoRepository;
    @Autowired
    CrawlerService crawlerService;
    @Autowired
    SkipDateService skipDateService;
    @Autowired
    RedisUtility redisUtility;

    private String today = LocalDate.now().toString();

    @Async("stockInfoExecutor")
    public StockBasicInfo asyncBuildStockBasicInfo(String stockcode) {
        return buildStockBasicInfo(stockcode);
    }

    public StockBasicInfo buildStockBasicInfo(String stockcode) {
        synchronized (LocalcacheService.getStockcodeLock(stockcode)) {
            return stockBasicInfoRepository.findByStockcode(stockcode)
                    .orElseGet(() -> {
                        StockBasicInfo stockBasicInfo = crawlerService.crawlStockBasicInfo(stockcode);
                        if (stockBasicInfo == null) {
                            return null;
                        } else if (!Stream.of("上市", "上櫃").collect(Collectors.toList()).contains(stockBasicInfo.getCompanyType())) {
                            redisUtility.valueSet(RedisConstant.BLACKLIST + stockcode, stockcode);
                        }
                        try {
                            stockBasicInfoRepository.save(stockBasicInfo);
                            log.info("股票基本資料: {} 寫入mongodb成功", stockcode);
                            return stockBasicInfo;
                        } catch (Exception e) {
                            log.warn("股票代碼:{} 寫入mongodb失敗 class:{} error:{}", stockcode, getClass().getName(), e.getMessage());
                            return null;
                        }
                    });
        }
    }

    @Async("stockExecutor")
    public StockDetail asyncBuildStockDetail(String stockcode) {
        return buildStockDetail(stockcode);
    }

    public StockDetail buildStockDetail(String stockcode) {
        LocalDate date = LocalDate.now();
        while (skipDateService.isSkipDate(date)) {
            date = date.minusDays(1);
        }

        synchronized (LocalcacheService.getStockcodeLock(stockcode)) {
            return stockDetailRepository.findByStockcodeAndCreatedTime(stockcode, date)
                    .orElseGet(() -> {
                        StockDetail stockDetail = crawlerService.crawlStock(stockcode);
                        if (stockDetail == null) {
                            return null;
                        }
                        try {
                            stockDetailRepository.save(stockDetail);
                            log.info("股票代碼:{} 交易日期:{} 寫入mongodb成功", stockcode, stockDetail.getCreatedTime());
                            redisUtility.valueObjectSet(RedisConstant.STOCKDETAIL + today + ":" + stockcode, stockDetail, 90, TimeUnit.DAYS);
                            try {
                                stockDetail.setId(stockDetail.getCreatedTime().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + stockcode);
                                stockDetailMapper.insertStockDetail(stockDetail);
                                log.info("股票代碼:{} 交易日期:{} 寫入mysql成功", stockcode, stockDetail.getCreatedTime());
                            } catch (Exception e) {
                                log.warn("股票代碼:{} 寫入mysql失敗 class:{} error:{}", stockcode, getClass().getName(), e.getMessage());
                            }
                            return stockDetail;
                        } catch (Exception e) {
                            log.warn("股票代碼:{} 寫入mongodb失敗 class:{} error:{}", stockcode, getClass().getName(), e.getMessage());
                            redisUtility.valueSet(RedisConstant.BLACKLIST + stockcode, stockcode, 3, TimeUnit.DAYS);
                            return null;
                        }
                    });
        }
    }

    //GET
    public Optional<StockDetail> getStockDetailToday(String stockcode) {
        return getStockDetailLast_n_day(stockcode, 0);
    }

    public Optional<StockDetail> getStockDetailLast_n_day(String stockcode, int days) {
        LocalDate localDate = LocalDate.now().minusDays(days);
        while (skipDateService.isSkipDate(localDate)) {
            localDate = localDate.minusDays(1);
        }
        String key = RedisConstant.STOCKDETAIL + localDate + ":" + stockcode;

        if (redisUtility.hasKey(key)) {
            log.info("取得 StockDetail: {} redis資訊", stockcode);
            StockDetail stockDetail = (StockDetail) redisUtility.valueObjectGet(key, StockDetail.class);
            return Optional.of(stockDetail);
        } else {
            synchronized (LocalcacheService.getStockcodeLock(stockcode)) {
                if (redisUtility.hasKey(key)) {
                    log.info("取得 StockDetail: {} redis synchronized資訊", stockcode);
                    StockDetail stockDetail = (StockDetail) redisUtility.valueObjectGet(key, StockDetail.class);
                    return Optional.of(stockDetail);
                } else {
                    log.info("取得 StockDetail: {} mongodb資訊", stockcode);
                    Optional<StockDetail> stockDetailOptional = stockDetailRepository.findByStockcodeAndCreatedTime(stockcode, localDate);
                    stockDetailOptional.ifPresent(stockDetail -> redisUtility.valueObjectSet(key, stockDetail, 90, TimeUnit.DAYS));
                    return stockDetailOptional;
                }
            }
        }
    }

    public Set<String> getStockSetByCompanyType(String companyType) {
        return stockBasicInfoRepository.findByCompanyType(companyType)
                .stream()
                .map(stockBasicInfo -> stockBasicInfo.getStockcode())
                .collect(Collectors.toSet());
    }

    public StockBasicInfo getStockBasicInfo(String stockcode) {
        String key = RedisConstant.STOCKBASICINFO + stockcode;

        if (redisUtility.hasKey(key)) {
            return (StockBasicInfo) redisUtility.valueObjectGet(key, StockBasicInfo.class);
        } else {
            synchronized (LocalcacheService.getStockcodeLock(stockcode)) {
                if (redisUtility.hasKey(key)) {
                    return (StockBasicInfo) redisUtility.valueObjectGet(key, StockBasicInfo.class);
                }

                StockBasicInfo stockBasicInfo = stockBasicInfoRepository.findByStockcode(stockcode).orElse(new StockBasicInfo());
                return stockBasicInfo;
            }
        }
    }

    public List<StockDetail> getStockDetailBetween(String stockcode, LocalDate startDate, LocalDate endDate) {
        return stockDetailRepository.findByStockcodeAndCreatedTimeBetweenOrderByCreatedTimeDesc(stockcode, startDate, endDate);
    }

    public String getCompanyType(String stockcode) {
        return getStockBasicInfo(stockcode).getCompanyType();
    }

    public void deleteAllStockDetail() {
        stockDetailRepository.deleteAll();
    }

    public boolean isListed(String stockcode) {
        return getCompanyType(stockcode).equals("上市") ? true : false;
    }

    //over-the-counter
    public boolean isOTC(String stockcode) {
        return getCompanyType(stockcode).equals("上櫃") ? true : false;
    }

    public boolean isEmerging(String stockcode) {
        return getCompanyType(stockcode).equals("興櫃") ? true : false;
    }
}
