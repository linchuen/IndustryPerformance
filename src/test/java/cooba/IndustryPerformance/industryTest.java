package cooba.IndustryPerformance;

import cooba.IndustryPerformance.constant.UrlEnum;
import cooba.IndustryPerformance.service.IndustryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class industryTest {
    @Autowired
    private IndustryService industryService;

    @Test
    public void getIndustryStockInfoTest() {
        industryService.getIndustryStockInfo(UrlEnum.金融.name());
    }

    @Test
    public void buildIndustryStockDetailInfoTest() {
        industryService.buildIndustryStockDetailInfo(UrlEnum.醫療器材.name());
    }

    @Test
    public void buildIndustryInfoTest() {
        //industryService.biuldAllIndustryInfo();
        industryService.buildIndustryInfo(UrlEnum.金融.name(), UrlEnum.金融.getUrl());
    }

    @Test
    public void getIndustryGrowthTest() {
        System.out.println(industryService.getIndustryGrowth(UrlEnum.金融.name()));
    }
}
