package cooba.IndustryPerformance;

import cooba.IndustryPerformance.service.rssImpl.Ettoday;
import cooba.IndustryPerformance.service.rssImpl.Finance;
import cooba.IndustryPerformance.service.rssImpl.LTN;
import cooba.IndustryPerformance.service.rssImpl.Technews;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class RssTest {
    @Autowired
    Technews technews;
    @Autowired
    LTN ltn;
    @Autowired
    Finance finance;
    @Autowired
    Ettoday ettoday;

    @Test
    public void technews() {
        technews.subscribe(technews.getUrl());
    }

    @Test
    public void ltn() {
        ltn.subscribe(ltn.getUrl());
    }

    @Test
    public void finance() {
        finance.subscribe(finance.getUrl());
    }

    @Test
    public void ettoday() {
        ettoday.subscribe(ettoday.getUrl());
    }
}
