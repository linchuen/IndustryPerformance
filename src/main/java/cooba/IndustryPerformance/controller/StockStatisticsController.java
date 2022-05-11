package cooba.IndustryPerformance.controller;

import cooba.IndustryPerformance.constant.KafkaConstant;
import cooba.IndustryPerformance.entity.StatisticsInfo;
import cooba.IndustryPerformance.utility.KafkaSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static cooba.IndustryPerformance.service.LocalcacheService.getListedStockList;

@RestController
public class StockStatisticsController {
    @Autowired
    KafkaSender kafkaSender;

    @GetMapping("statistics/{stockcode}/{startDate}/{endDate}")
    public void calculateStockStatistics(@PathVariable String stockcode, @PathVariable String startDate, @PathVariable String endDate) {
        StatisticsInfo statisticsInfo = StatisticsInfo.builder()
                .stockcode(stockcode)
                .startDate(LocalDate.parse(startDate, DateTimeFormatter.ofPattern("yyyyMMdd")))
                .endDate(LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyyMMdd")))
                .build();
        kafkaSender.send(KafkaConstant.STATISTICSTOPIC, statisticsInfo);
    }

    @GetMapping("statistics/All")
    public List<String> calculateStockStatistics(@RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate) {
        LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.ofPattern("yyyyMMdd"));
        LocalDate end = LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyyMMdd"));
        List<String> sendList = new ArrayList<>();
        getListedStockList().forEach(stockcode -> {
            StatisticsInfo statisticsInfo = StatisticsInfo.builder()
                    .stockcode(stockcode)
                    .startDate(start)
                    .endDate(end)
                    .build();
            kafkaSender.send(KafkaConstant.STATISTICSTOPIC, statisticsInfo);
            sendList.add(stockcode);
        });

        return sendList;
    }
}
