package cooba.IndustryPerformance.Scheduler;

import cooba.IndustryPerformance.enums.UrlEnum;
import cooba.IndustryPerformance.service.IndustryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ScheduleService {
    @Autowired
    IndustryService industryService;

    @Scheduled(cron = "0 0 0 1 1-12 *")
    private void biuldAllIndustryInfo() {
        industryService.biuldAllIndustryInfo();
    }

    @Scheduled(cron = "0 0 10  * * *")
    private void buildIndustryStockDetailInfo() throws InterruptedException {
        for (UrlEnum urlEnum : UrlEnum.values()) {
            industryService.buildIndustryStockDetailInfo(urlEnum.name());
        }
    }
}
