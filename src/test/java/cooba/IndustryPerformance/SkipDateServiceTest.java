package cooba.IndustryPerformance;

import cooba.IndustryPerformance.service.SkipDateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

@SpringBootTest
public class SkipDateServiceTest {
    @Autowired
    SkipDateService skipDateService;

    @Test
    public void createSkipDate() {
        skipDateService.createSkipDate(LocalDate.now().getYear());
    }
}
