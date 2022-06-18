package cooba.IndustryPerformance.controller;

import cooba.IndustryPerformance.database.entity.EvaluateEntity.EvaluateEntity;
import cooba.IndustryPerformance.service.EvaluateService;
import cooba.IndustryPerformance.service.TimeCounterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static cooba.IndustryPerformance.service.LocalcacheService.getListedStockList;

@RestController
public class EvaluateController {
    @Autowired
    TimeCounterService timeCounterService;
    @Autowired
    EvaluateService evaluateService;

    @GetMapping("evaluate/All/{year}/{month}")
    public List<String> creatAllEvaluateEntity(@PathVariable String year, @PathVariable String month) {
        List<String> sendList = new ArrayList<>();
        String uuid = timeCounterService.createTimeCounter("creatAllEvaluateEntity", "計算上市股票評估數據 計算月份:" + year + month);
        getListedStockList().forEach(stockcode -> {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            try {
                EvaluateEntity evaluateEntity = evaluateService.createEvaluateEntity(null, Integer.parseInt(year), Integer.parseInt(month), stockcode);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(stockcode);
            }
            stopWatch.stop();
            timeCounterService.addTime(uuid, stopWatch.getTotalTimeSeconds());
            sendList.add(stockcode);
        });
        return sendList;
    }
}
