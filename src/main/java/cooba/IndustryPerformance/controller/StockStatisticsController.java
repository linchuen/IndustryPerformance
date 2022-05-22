package cooba.IndustryPerformance.controller;

import cooba.IndustryPerformance.constant.KafkaConstant;
import cooba.IndustryPerformance.entity.StatisticsInfo;
import cooba.IndustryPerformance.entity.StockDetailStatistics;
import cooba.IndustryPerformance.service.StockStatisticsService;
import cooba.IndustryPerformance.utility.KafkaSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static cooba.IndustryPerformance.service.LocalcacheService.getListedStockList;

@RestController
public class StockStatisticsController {
    @Autowired
    KafkaSender kafkaSender;
    @Autowired
    StockStatisticsService stockStatisticsService;

    @GetMapping("statistics/{stockcode}/{startDate}/{endDate}")
    public ResponseEntity calculateStockStatistics(@PathVariable String stockcode, @PathVariable String startDate, @PathVariable String endDate) {
        StatisticsInfo statisticsInfo = StatisticsInfo.builder()
                .stockcode(stockcode)
                .startDate(LocalDate.parse(startDate, DateTimeFormatter.ofPattern("yyyyMMdd")))
                .endDate(LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyyMMdd")))
                .build();
        kafkaSender.send(KafkaConstant.STATISTICSTOPIC, statisticsInfo);
        return ResponseEntity.ok().build();
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

    @PutMapping("statistics/AllBefore")
    public List<String> calculateStockStatisticsDateBefore(@RequestParam("startDate") String startDate) {
        LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.ofPattern("yyyyMMdd"));
        List<String> sendList = new ArrayList<>();
        getListedStockList().forEach(stockcode -> {
            StatisticsInfo statisticsInfo = StatisticsInfo.builder()
                    .stockcode(stockcode)
                    .startDate(start)
                    .build();
            kafkaSender.send(KafkaConstant.STATISTICSTOPIC, statisticsInfo);
            sendList.add(stockcode);
        });

        return sendList;
    }

    @GetMapping("statistics/{stockcode}")
    public StockDetailStatistics getStockDetailStatistics(@PathVariable String stockcode, @RequestParam("date") String date) {
        return stockStatisticsService.getStockcodeStatistics(stockcode, LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyyMMdd")));
    }

    @GetMapping("statistics/{stockcode}/{limit}")
    public List<StockDetailStatistics> getStockDetailStatisticsList(@PathVariable String stockcode, @PathVariable int limit) {
        return stockStatisticsService.getStockcodeStatisticsList(stockcode, limit);
    }
}
