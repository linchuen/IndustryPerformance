package cooba.IndustryPerformance.controller;

import cooba.IndustryPerformance.constant.KafkaConstant;
import cooba.IndustryPerformance.entity.StatisticsInfo;
import cooba.IndustryPerformance.entity.StockDetailStatistics;
import cooba.IndustryPerformance.service.StockStatisticsService;
import cooba.IndustryPerformance.service.TimeCounterService;
import cooba.IndustryPerformance.utility.KafkaSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static cooba.IndustryPerformance.constant.CommonConstant.YMD;
import static cooba.IndustryPerformance.service.LocalcacheService.getListedStockList;

@RestController
public class StockStatisticsController {
    @Autowired
    KafkaSender kafkaSender;
    @Autowired
    StockStatisticsService stockStatisticsService;
    @Autowired
    TimeCounterService timeCounterService;

    @GetMapping("statistics/{stockcode}/{startDate}/{endDate}")
    public ResponseEntity calculateStockStatistics(@PathVariable String stockcode, @PathVariable String startDate, @PathVariable String endDate) {
        String uuid = timeCounterService.createTimeCounter("calculateStockStatistics", "股票:" + stockcode + " 開始時間:" + startDate + " 結束時間:" + endDate);
        StatisticsInfo statisticsInfo = StatisticsInfo.builder()
                .stockcode(stockcode)
                .uuid(uuid)
                .startDate(LocalDate.parse(startDate, YMD))
                .endDate(LocalDate.parse(endDate, YMD))
                .build();
        kafkaSender.send(KafkaConstant.STATISTICSTOPIC, statisticsInfo);
        return ResponseEntity.ok().body("發送Kafka成功");
    }

    @GetMapping("statistics/All")
    public List<String> calculateStockStatistics(@RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate) {
        LocalDate start = LocalDate.parse(startDate, YMD);
        LocalDate end = LocalDate.parse(endDate, YMD);
        List<String> sendList = new ArrayList<>();
        String uuid = timeCounterService.createTimeCounter("calculateStockStatistics", "計算上市股票 開始時間:" + startDate + " 結束時間:" + endDate);
        getListedStockList().forEach(stockcode -> {
            StatisticsInfo statisticsInfo = StatisticsInfo.builder()
                    .stockcode(stockcode)
                    .uuid(uuid)
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
        LocalDate start = LocalDate.parse(startDate, YMD);
        List<String> sendList = new ArrayList<>();
        String uuid = timeCounterService.createTimeCounter("calculateStockStatisticsDateBefore", "計算開始時間" + startDate + "之前上市股票");
        getListedStockList().forEach(stockcode -> {
            StatisticsInfo statisticsInfo = StatisticsInfo.builder()
                    .stockcode(stockcode)
                    .uuid(uuid)
                    .startDate(start)
                    .build();
            kafkaSender.send(KafkaConstant.STATISTICSTOPIC, statisticsInfo);
            sendList.add(stockcode);
        });

        return sendList;
    }

    @GetMapping("statistics/{stockcode}")
    public StockDetailStatistics getStockDetailStatistics(@PathVariable String stockcode, @RequestParam("date") String date) {
        return stockStatisticsService.getStockcodeStatistics(stockcode, LocalDate.parse(date, YMD));
    }

    @GetMapping("statistics/{stockcode}/{limit}")
    public List<StockDetailStatistics> getStockDetailStatisticsList(@PathVariable String stockcode, @PathVariable int limit) {
        List<StockDetailStatistics> resultList = stockStatisticsService.getStockcodeStatisticsList(stockcode, limit);
        return resultList;
    }
}
