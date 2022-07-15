package cooba.IndustryPerformance.Task;

import cooba.IndustryPerformance.constant.KafkaConstant;
import cooba.IndustryPerformance.entity.StockCsvInfo;
import cooba.IndustryPerformance.service.IndustryService;
import cooba.IndustryPerformance.service.SkipDateService;
import cooba.IndustryPerformance.service.TimeCounterService;
import cooba.IndustryPerformance.service.rssImpl.RssService;
import cooba.IndustryPerformance.utility.KafkaSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

import static cooba.IndustryPerformance.service.LocalcacheService.getListedStockList;

@Service
public class ScheduleService {
    @Autowired
    IndustryService industryService;
    @Autowired
    KafkaSender kafkaSender;
    @Autowired
    SkipDateService skipDateService;
    @Autowired
    TimeCounterService timeCounterService;
    @Autowired
    List<? extends RssService> rssServices;

    @Scheduled(cron = "0 0 0 1 * *")
    private void biuldAllIndustryInfo() {
        industryService.biuldAllIndustryInfo();
    }

    @Scheduled(cron = "0 0 15 * * *")
    private void buildIndustryStockDetailInfo() {
        industryService.buildtodayStockDetail();
    }

    @Scheduled(cron = "0 0 0 1 * *")
    private void biuldLastMonthHistoryStockDetail() {
        LocalDate lastMonth = LocalDate.now().minusMonths(1);
        String uuid = timeCounterService.createTimeCounter("biuldLastMonthHistoryStockDetail", "每月執行下載上個月歷史資料");
        getListedStockList().forEach(stockcode -> {
            StockCsvInfo stockCsvInfo = new StockCsvInfo(stockcode, uuid, lastMonth);
            kafkaSender.send(KafkaConstant.HISTORYSTOCKDETAILTOPIC, stockCsvInfo);
        });
    }

    @Scheduled(cron = "0 0 0 1 1 *")
    private void biuldSkipDate() {
        skipDateService.downloadSkipDateCsv(LocalDate.now().getYear());
        skipDateService.createSkipDate(LocalDate.now().getYear());
    }

    @Scheduled(cron = "0 0 1 * * *")
    private void news() {
        rssServices.forEach(rssService -> rssService.subscribe(rssService.getUrl()));
    }
}
