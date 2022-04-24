package cooba.IndustryPerformance.scheduler;

import cooba.IndustryPerformance.constant.KafkaConstant;
import cooba.IndustryPerformance.database.entity.Industry.Industry;
import cooba.IndustryPerformance.entity.StockCsvInfo;
import cooba.IndustryPerformance.service.IndustryService;
import cooba.IndustryPerformance.service.SkipDateService;
import cooba.IndustryPerformance.utility.KafkaSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ScheduleService {
    @Autowired
    IndustryService industryService;
    @Autowired
    KafkaSender kafkaSender;
    @Autowired
    SkipDateService skipDateService;

    @Scheduled(cron = "0 0 0 1 * *")
    private void biuldAllIndustryInfo() {
        industryService.biuldAllIndustryInfo();
    }

    @Scheduled(cron = "0 0 10 * * *")
    private void buildIndustryStockDetailInfo() {
        industryService.buildtodayStockDetail();
    }

    @Scheduled(cron = "0 0 0 1 * *")
    private void biuldLastMonthHistoryStockDetail() {
        LocalDate lastMonth = LocalDate.now().minusMonths(1);
        List<Industry> industryList = industryService.getAllIndustry();
        industryList.forEach(industry -> industry.getSubIndustries()
                .forEach(subIndustry -> subIndustry.getCompanies()
                        .forEach(stock -> {
                            StockCsvInfo stockCsvInfo = new StockCsvInfo(stock.getStockcode(), lastMonth);
                            kafkaSender.send(KafkaConstant.HISTORYSTOCKDETAILTOPIC, stockCsvInfo);
                        })
                )
        );
    }

    @Scheduled(cron = "0 0 0 1 1 *")
    private void biuldSkipDate() {
        skipDateService.downloadSkipDateCsv();
        skipDateService.createSkipDate();
    }
}
