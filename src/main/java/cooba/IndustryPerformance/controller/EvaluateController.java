package cooba.IndustryPerformance.controller;

import cooba.IndustryPerformance.database.entity.EvaluateEntity.EvaluateEntity;
import cooba.IndustryPerformance.entity.StockTopRankEntity;
import cooba.IndustryPerformance.service.EvaluateService;
import cooba.IndustryPerformance.service.TimeCounterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cooba.IndustryPerformance.constant.CommonConstant.YM;
import static cooba.IndustryPerformance.service.LocalcacheService.getListedStockList;

@RestController
public class EvaluateController {
    @Autowired
    TimeCounterService timeCounterService;
    @Autowired
    EvaluateService evaluateService;

    @GetMapping("evaluate/All/{year}/{month}")
    public List<String> createAllEvaluateEntity(@PathVariable String year, @PathVariable String month) {
        List<String> sendList = new ArrayList<>();
        String uuid = timeCounterService.createTimeCounter("creatAllEvaluateEntity", "計算上市股票評估數據 計算月份:" + year + month);
        getListedStockList().forEach(stockcode -> {
            evaluateService.createEvaluateEntityAsync(uuid, null, Integer.parseInt(year), Integer.parseInt(month), stockcode);
            sendList.add(stockcode);
        });
        return sendList;
    }

    @GetMapping("evaluate/{stockcode}")
    public EvaluateEntity readStockEvaluateEntity(@PathVariable String stockcode) {
        String dateStr = LocalDate.now().format(YM);
        return evaluateService.readStockEvaluateEntity(stockcode, dateStr);
    }

    @GetMapping("evaluate/topRank/{year}/{month}")
    public List<Map<String, StockTopRankEntity>> getEvaluateMainList(@PathVariable String year, @PathVariable String month) {
        return evaluateService.getEvaluateMainList(Integer.parseInt(year), Integer.parseInt(month));
    }

    @GetMapping("evaluate/main/{year}/{month}")
    public void evaluateMain(@PathVariable String year, @PathVariable String month) {
        evaluateService.evaluateMain(Integer.parseInt(year), Integer.parseInt(month));
    }
}
