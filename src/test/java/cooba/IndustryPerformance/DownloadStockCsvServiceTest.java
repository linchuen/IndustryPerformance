package cooba.IndustryPerformance;


import cooba.IndustryPerformance.service.DownloadStockCsvService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

@SpringBootTest
public class DownloadStockCsvServiceTest {
    @Autowired
    DownloadStockCsvService downloadStockCsvService;

    @Test
    public void Test() {
        //downloadStockCsvService.downloadStockCsv("2330", LocalDate.of(2022, 3, 1));
        downloadStockCsvService.readCsvToDB("2330", LocalDate.of(2022, 3, 1));
    }
}
