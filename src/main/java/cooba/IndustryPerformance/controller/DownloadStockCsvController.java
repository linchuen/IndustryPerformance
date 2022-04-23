package cooba.IndustryPerformance.controller;

import cooba.IndustryPerformance.constant.KafkaConstant;
import cooba.IndustryPerformance.entity.StockCsvInfo;
import cooba.IndustryPerformance.service.IndustryService;
import cooba.IndustryPerformance.utility.KafkaSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

@RestController
public class DownloadStockCsvController {
    @Autowired
    IndustryService industryService;
    @Autowired
    KafkaSender kafkaSender;

    @GetMapping("historycsv/{industryType}")
    public String setIndustryHistoryStockDetail(@PathVariable String industryType) {
        Map<String, String> industryStockMap = industryService.getIndustryStockInfo(industryType);
        industryStockMap.entrySet().forEach(entry -> {
            String stockcode = entry.getKey();
            String name = entry.getValue();
            StockCsvInfo stockCsvInfo = new StockCsvInfo(stockcode, LocalDate.now());
            kafkaSender.send(KafkaConstant.HISTORYSTOCKDETAILTOPIC, stockCsvInfo);
        });
        return String.format("%s 已經送至kafka", industryType);
    }

    @GetMapping("historycsv/{industryType}/{subIndustryName}")
    public String setSubindustryHistoryStockDetail(@PathVariable String industryType, @PathVariable String subIndustryName) {
        Map<String, String> industryStockMap = industryService.getSubIndustryStockInfo(industryType, subIndustryName);
        industryStockMap.entrySet().forEach(entry -> {
            String stockcode = entry.getKey();
            String name = entry.getValue();
            StockCsvInfo stockCsvInfo = new StockCsvInfo(stockcode, LocalDate.now());
            kafkaSender.send(KafkaConstant.HISTORYSTOCKDETAILTOPIC, stockCsvInfo);
        });
        return String.format("%s %s 已經送至kafka", industryType, subIndustryName);
    }
}
