package cooba.IndustryPerformance.service;

import com.opencsv.CSVReader;
import cooba.IndustryPerformance.database.entity.SkipDate.SkipDate;
import cooba.IndustryPerformance.database.repository.SkipDateRepository;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class SkipDateService {
    @Value("${stock.csv.path}")
    private String csvPath;
    @Autowired
    SkipDateRepository skipDateRepository;

    public void downloadSkipDateCsv(int year) {
        year = year - 1911;
        try {
            WebDriverManager.chromedriver().setup();
            HashMap<String, Object> chromePrefs = new HashMap<String, Object>();
            chromePrefs.put("download.default_directory", csvPath);
            ChromeOptions chromeOptions = new ChromeOptions();
            chromeOptions.addArguments("--headless");
            chromeOptions.addArguments("--disable-gpu");
            chromeOptions.setExperimentalOption("prefs", chromePrefs);
            WebDriver driver = new ChromeDriver(chromeOptions);
            driver.get(String.format("https://www.twse.com.tw/holidaySchedule/holidaySchedule?response=csv&queryYear=%d", year));
            Thread.sleep(1000);
            driver.quit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean createSkipDate(int year) {
        int chineseYear = year - 1911;
        String filePath = String.format("%s\\holidaySchedule_%d.csv", csvPath, chineseYear);
        try {
            CSVReader openCSVReader = new CSVReader(new InputStreamReader(new FileInputStream(filePath), "Big5"));
            List<String[]> list = openCSVReader.readAll();
            list = list.subList(2, list.size() - 1);
            list.forEach(strings -> {
                int[] dateArr = Arrays.stream(strings[1].replace("日", "").split("月")).mapToInt(Integer::parseInt).toArray();
                LocalDate date = LocalDate.of(year, dateArr[0], dateArr[1]);
                skipDateRepository.save(new SkipDate(date));
            });
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isSkipDate(LocalDate date) {
        List weekend = Stream.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY).collect(Collectors.toList());
        if (weekend.contains(date.getDayOfWeek())) {
            return true;
        }
        if (!LocalcacheService.getSkipDateList().isEmpty()) {
            return LocalcacheService.getSkipDateList().contains(date);
        }
        return skipDateRepository.findBySkipDate(date).isPresent();
    }
}

