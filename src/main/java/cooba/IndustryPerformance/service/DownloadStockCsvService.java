package cooba.IndustryPerformance.service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import cooba.IndustryPerformance.database.entity.StockDetail.StockDetail;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DownloadStockCsvService {

    public static void downloadCsv(String stockcode, LocalDate date) {
        try {
            WebDriverManager.chromedriver().setup();
            ChromeOptions chromeOptions = new ChromeOptions();
            chromeOptions.addArguments("--headless");
            chromeOptions.addArguments("--disable-gpu");
            WebDriver driver = new ChromeDriver(chromeOptions);
            String dateStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            driver.get(String.format("https://www.twse.com.tw/exchangeReport/STOCK_DAY?response=csv&date=%s&stockNo=%s", dateStr, stockcode));
            Thread.sleep(300);
            driver.quit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void readCsv(String stockcode, String date) {
        String filePath = String.format("C:\\Users\\Lin\\Downloads\\STOCK_DAY_%s_%s.csv", stockcode, date);
        try {
            CSVReader openCSVReader = new CSVReader(new InputStreamReader(new FileInputStream(filePath), "Big5"));
            String[] title = openCSVReader.readNext()[0].split(" ");
            String name = title[2];
            System.out.println(name);
            try {
                List<String[]> list = openCSVReader.readAll();
                list = list.subList(2, list.size() - 5);
                list.forEach(records -> {
                    BigDecimal price = BigDecimal.valueOf(Float.valueOf(records[6].replace(",", "")));
                    BigDecimal lastprice = BigDecimal.valueOf(Float.valueOf(records[6].replace(",", "")));
                    BigDecimal open = BigDecimal.valueOf(Float.valueOf(records[3].replace(",", "")));
                    BigDecimal highest = BigDecimal.valueOf(Float.valueOf(records[4].replace(",", "")));
                    BigDecimal lowest = BigDecimal.valueOf(Float.valueOf(records[5].replace(",", "")));
                    Long sharesTraded = Long.parseLong(records[1].replace(",", ""));
                    Long turnover = Long.parseLong(records[2].replace(",", ""));
                    int tradingVolume = Integer.parseInt(records[8].replace(",", ""));
                    List<Integer> dateList = Arrays.stream(records[0].split("/"))
                            .map(s -> Integer.parseInt(s))
                            .collect(Collectors.toList());
                    //dateList格視為111/03/01轉換來的
                    LocalDate createdTime = LocalDate.of(dateList.get(0) + 2011, dateList.get(1), dateList.get(2));

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
                            .build();
                });
            } catch (CsvException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
//        LocalDate date = LocalDate.of(2022, 3, 1);
//        downloadCsv("2884", date);
        readCsv("2884", "202203");
    }

}
