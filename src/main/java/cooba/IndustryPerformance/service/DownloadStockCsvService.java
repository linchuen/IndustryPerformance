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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
    @Autowired
    TimeCounterService timeCounterService;

    public boolean downloadStockCsv(String stockcode, LocalDate date) {
        String filePath = String.format("%s\\STOCK_DAY_%s_%s.csv", csvPath, stockcode, date.format(DateTimeFormatter.ofPattern("yyyyMM")));
        File file = new File(filePath);
        if (!file.exists()) {
            WebDriverManager.chromedriver().setup();
            HashMap<String, Object> chromePrefs = new HashMap<String, Object>();
            chromePrefs.put("download.default_directory", csvPath);
            ChromeOptions chromeOptions = new ChromeOptions();
            chromeOptions.addArguments("--headless");
            chromeOptions.addArguments("--disable-gpu");
            chromeOptions.setExperimentalOption("prefs", chromePrefs);
            WebDriver driver = new ChromeDriver(chromeOptions);
            try {
                String dateStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                driver.get(String.format("https://www.twse.com.tw/exchangeReport/STOCK_DAY?response=csv&date=%s&stockNo=%s", dateStr, stockcode));
                Thread.sleep(2000);
                driver.quit();
                if (file.exists()) {
                    log.info("downloadCsv成功 {}", file.getName());
                    return true;
                }
                return false;
            } catch (Exception e) {
                driver.quit();
                log.warn("downloadCsv失敗 class:{} error:{}", getClass().getName(), e.getMessage());
                return false;
            }
        }
        return true;
    }

    @Async("stockExecutor")
    public void readCsvToDBAsync(String uuid, String stockcode, LocalDate date) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("readCsvToDBAsync" + stockcode + date);
        readCsvToDB(stockcode, date);
        stopWatch.stop();
        System.out.println(stopWatch.prettyPrint());
        Double time = stopWatch.getTotalTimeSeconds();
        timeCounterService.addTime(uuid, time);
    }

    @Transactional(rollbackFor = Exception.class)
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
            list = list.subList(1, list.size() - 5);
            String finalIndustryType = industryType;
            String finalCompanyType = companyType;
            list.forEach(records -> {
                BigDecimal price = new BigDecimal(records[6].replace(",", "").replace("--", "0"));
                BigDecimal lastprice = new BigDecimal(records[6].replace(",", "").replace("--", "0"));
                BigDecimal open = new BigDecimal(records[3].replace(",", "").replace("--", "0"));
                BigDecimal highest = new BigDecimal(records[4].replace(",", "").replace("--", "0"));
                BigDecimal lowest = new BigDecimal(records[5].replace(",", "").replace("--", "0"));
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
                        .joinKey(createdTime.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + stockcode)
                        .build();
                stockDetailRepository.findByStockcodeAndCreatedTime(stockcode, createdTime).ifPresentOrElse(
                        oldStockDetail -> {
                            stockDetail.setId(oldStockDetail.getId());
                            stockDetailRepository.save(stockDetail);
                        },
                        () -> {
                            try {
                                stockDetailRepository.save(stockDetail);
                            } catch (Exception e) {
                                log.warn("股票代碼:{} 寫入mongodb失敗 class:{} error:{}", stockcode, getClass().getName(), e.getMessage());
                            }
                        }
                );

                try {
                    stockDetail.setId(createdTime.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + stockcode);
                    stockDetailMapper.insertStockDetail(stockDetail);
                } catch (Exception e) {
                    log.warn("股票代碼:{} 寫入mysql失敗 class:{} error:{}", stockcode, getClass().getName(), e.getMessage());
                }
            });
            log.info("股票代碼:{} readCsvToDB成功", stockcode);
        } catch (Exception e) {
            log.warn("股票代碼:{} readCsvToDB失敗 class:{} error:{}", stockcode, getClass().getName(), e.getMessage());
        }
    }

    public List<String> organizeFile(LocalDate date) {
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyyMM"));
        String folderStr = csvPath + "\\" + dateStr;
        new File(folderStr).mkdir();

        List<String> changeList = new ArrayList<>();
        File csvFolder = new File(csvPath);
        for (String file : csvFolder.list()) {
            if (file.contains(dateStr)) {
                try {
                    File from = new File(csvPath + "\\" + file);
                    File to = new File(folderStr + "\\" + file);
                    if (from.isFile()) {
                        Files.move(from.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        changeList.add(file);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return changeList;
    }
}
