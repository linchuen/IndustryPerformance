package cooba.IndustryPerformance.controller;

import cooba.IndustryPerformance.constant.KafkaConstant;
import cooba.IndustryPerformance.entity.StockCsvInfo;
import cooba.IndustryPerformance.service.DownloadStockCsvService;
import cooba.IndustryPerformance.service.IndustryService;
import cooba.IndustryPerformance.service.TimeCounterService;
import cooba.IndustryPerformance.utility.KafkaSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class DownloadStockCsvController {
    @Autowired
    IndustryService industryService;
    @Autowired
    DownloadStockCsvService downloadStockCsvService;
    @Autowired
    KafkaSender kafkaSender;
    @Autowired
    TimeCounterService timeCounterService;

    @GetMapping("historycsv/{industryType}")
    public String setIndustryHistoryStockDetail(@PathVariable String industryType) {
        Map<String, String> industryStockMap = industryService.getIndustryStockInfo(industryType);
        String uuid = timeCounterService.createTimeCounter("setIndustryHistoryStockDetail", "下載" + industryType + LocalDate.now() + "歷史資料");
        industryStockMap.entrySet().forEach(entry -> {
            String stockcode = entry.getKey();
            StockCsvInfo stockCsvInfo = new StockCsvInfo(stockcode, uuid, LocalDate.now());
            kafkaSender.send(KafkaConstant.HISTORYSTOCKDETAILTOPIC, stockCsvInfo);
        });
        return String.format("%s 已經送至kafka", industryType);
    }

    @GetMapping("historycsv/{industryType}/{subIndustryName}")
    public String setSubindustryHistoryStockDetail(@PathVariable String industryType, @PathVariable String subIndustryName) {
        Map<String, String> industryStockMap = industryService.getSubIndustryStockInfo(industryType, subIndustryName);
        String uuid = timeCounterService.createTimeCounter("setIndustryHistoryStockDetail", "下載" + industryType + subIndustryName + LocalDate.now() + "歷史資料");
        industryStockMap.entrySet().forEach(entry -> {
            String stockcode = entry.getKey();
            StockCsvInfo stockCsvInfo = new StockCsvInfo(stockcode, uuid, LocalDate.now());
            kafkaSender.send(KafkaConstant.HISTORYSTOCKDETAILTOPIC, stockCsvInfo);
        });
        return String.format("%s %s 已經送至kafka", industryType, subIndustryName);
    }

    //date格視為yyyyMMdd
    @GetMapping("historycsv")
    public String setAllIndustryHistoryStockDetail(@RequestParam("date") String dateStr) {
        LocalDate date = LocalDate.parse(dateStr);
        try {
            String uuid = timeCounterService.createTimeCounter("setIndustryHistoryStockDetail", "下載全部" + date + "歷史資料");
            industryService.getAllIndustry()
                    .forEach(industry -> industryService.getIndustryStockInfo(industry.getIndustryName()).entrySet()
                            .forEach(entry -> {
                                String stockcode = entry.getKey();
                                StockCsvInfo stockCsvInfo = new StockCsvInfo(stockcode, uuid, date);
                                kafkaSender.send(KafkaConstant.HISTORYSTOCKDETAILTOPIC, stockCsvInfo);
                            }));
            return String.format("%s AllIndustryHistory已經送至kafka", date);
        } catch (Exception e) {
            return "發生錯誤";
        }
    }

    @GetMapping("organizefile")
    public List<String> organizeFile(@RequestParam("date") String dateStr) {
        LocalDate date = LocalDate.parse(dateStr);
        try {
            return downloadStockCsvService.organizeFile(date);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
