package cooba.IndustryPerformance.service;

import com.opencsv.CSVReader;
import cooba.IndustryPerformance.constant.RedisConstant;
import cooba.IndustryPerformance.database.entity.StockBasicInfo.StockBasicInfo;
import cooba.IndustryPerformance.database.entity.StockDetail.StockDetail;
import cooba.IndustryPerformance.database.mapper.StockDetailMapper;
import cooba.IndustryPerformance.database.repository.StockDetailRepository;
import cooba.IndustryPerformance.utility.RedisUtility;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DownloadStockCsvService {
    @Value("${stock.csv.path}")
    private String csvPath;
    @Autowired
    StockDetailRepository stockDetailRepository;
    @Autowired
    StockDetailMapper stockDetailMapper;
    @Autowired
    RedisUtility redisUtility;

    public boolean downloadStockCsv(String stockcode, LocalDate date) {
        String filePath = String.format("%s\\STOCK_DAY_%s_%s.csv", csvPath, stockcode, date.format(DateTimeFormatter.ofPattern("yyyyMM")));
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                WebDriverManager.chromedriver().setup();
                HashMap<String, Object> chromePrefs = new HashMap<String, Object>();
                chromePrefs.put("download.default_directory", csvPath);
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.addArguments("--headless");
                chromeOptions.addArguments("--disable-gpu");
                chromeOptions.setExperimentalOption("prefs", chromePrefs);
                WebDriver driver = new ChromeDriver(chromeOptions);
                String dateStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                driver.get(String.format("https://www.twse.com.tw/exchangeReport/STOCK_DAY?response=csv&date=%s&stockNo=%s", dateStr, stockcode));
                Thread.sleep(1000);
                driver.quit();
                if (file.exists()) {
                    log.info("downloadCsv成功 {}", file.getName());
                    return true;
                }
                return false;
            } catch (Exception e) {
                log.warn("downloadCsv失敗 class:{} error:{}", getClass().getName(), e.getMessage());
                return false;
            }
        }
        return true;
    }

    @Async("stockExecutor")
    public void readCsvToDBAsync(String stockcode, LocalDate date) {
        readCsvToDB(stockcode, date);
    }

    public void readCsvToDB(String stockcode, LocalDate date) {
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyyMM"));
        String filePath = String.format("%s\\STOCK_DAY_%s_%s.csv", csvPath, stockcode, dateStr);
        File file = new File(filePath);
        if (file.length() / 1024 == 0) return;

        try {
            CSVReader openCSVReader = new CSVReader(new InputStreamReader(new FileInputStream(filePath), "Big5"));
            String[] title = openCSVReader.readNext()[0].split(" ");
            String name = title[2];
            String key = RedisConstant.STOCKBASICINFO + stockcode;
            String industryType = "";
            String companyType = "";
            if (redisUtility.hasKey(key)) {
                StockBasicInfo stockBasicInfo = (StockBasicInfo) redisUtility.valueObjectGet(RedisConstant.STOCKBASICINFO + stockcode, StockBasicInfo.class);
                industryType = String.valueOf(stockBasicInfo.getIndustryType());
                companyType = String.valueOf(stockBasicInfo.getCompanyType());
            }
            List<String[]> list = openCSVReader.readAll();
            list = list.subList(2, list.size() - 5);
            String finalIndustryType = industryType;
            String finalCompanyType = companyType;
            list.forEach(records -> {
                BigDecimal price = new BigDecimal(Float.valueOf(records[6].replace(",", "")));
                BigDecimal lastprice = new BigDecimal(Float.valueOf(records[6].replace(",", "")));
                BigDecimal open = new BigDecimal(Float.valueOf(records[3].replace(",", "")));
                BigDecimal highest = new BigDecimal(records[4].replace(",", ""));
                BigDecimal lowest = new BigDecimal(Float.valueOf(records[5].replace(",", "")));
                Long sharesTraded = Long.parseLong(records[1].replace(",", ""));
                Long turnover = Long.parseLong(records[2].replace(",", ""));
                int tradingVolume = Integer.parseInt(records[8].replace(",", ""));
                List<Integer> dateList = Arrays.stream(records[0].split("/"))
                        .map(s -> Integer.parseInt(s))
                        .collect(Collectors.toList());
                //dateList格視為111/03/01轉換來的
                LocalDate createdTime = LocalDate.of(dateList.get(0) + 1911, dateList.get(1), dateList.get(2));

                StockDetail stockDetail = StockDetail.builder()
                        .stockcode(stockcode)
                        .name(name)
                        .price(price)
                        .lastprice(lastprice)
                        .open(open)
                        .highest(highest)
                        .lowest(lowest)
                        .sharesTraded(sharesTraded)
                        .turnover(turnover)
                        .tradingVolume(tradingVolume)
                        .createdTime(createdTime)
                        .industryType(finalIndustryType)
                        .companyType(finalCompanyType)
                        .build();
                stockDetailRepository.findByStockcodeAndCreatedTime(stockcode, createdTime).ifPresentOrElse(
                        oldStockDetail -> {
                            stockDetail.setId(oldStockDetail.getId());
                            stockDetailRepository.save(stockDetail);
                            log.info("股票代碼:{} 交易日期:{} 更新mongodb成功", stockcode, stockDetail.getCreatedTime());
                        },
                        () -> {
                            try {
                                stockDetailRepository.save(stockDetail);
                                log.info("股票代碼:{} 交易日期:{} 寫入mongodb成功", stockcode, stockDetail.getCreatedTime());
                            } catch (Exception e) {
                                log.warn("股票代碼:{} 寫入mongodb失敗 class:{} error:{}", stockcode, getClass().getName(), e.getMessage());
                            }
                        }
                );

                try {
                    stockDetail.setId(createdTime.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + stockcode);
                    stockDetailMapper.insertStockDetail(stockDetail);
                    log.info("股票代碼:{} 交易日期:{} 寫入mysql成功", stockcode, stockDetail.getCreatedTime());
                } catch (Exception e) {
                    log.warn("股票代碼:{} 寫入mysql失敗 class:{} error:{}", stockcode, getClass().getName(), e.getMessage());
                }
            });
        } catch (Exception e) {
            log.warn("{} readCsvToDB失敗 class:{} error:{}", stockcode, getClass().getName(), e.getMessage());
        }
    }
}
