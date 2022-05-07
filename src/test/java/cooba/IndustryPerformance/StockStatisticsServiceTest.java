package cooba.IndustryPerformance;

import cooba.IndustryPerformance.database.entity.StockDetail.StockDetail;
import cooba.IndustryPerformance.database.repository.StockDetailRepository;
import cooba.IndustryPerformance.service.StockStatisticsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class StockStatisticsServiceTest {
    @Autowired
    StockDetailRepository stockDetailRepository;
    @Autowired
    StockStatisticsService stockStatisticsService;

    @Test
    public void get() {
        List<StockDetail> stockDetailList = stockDetailRepository.findByStockcodeAndCreatedTimeBetween("2330", LocalDate.of(2022, 1, 1), LocalDate.of(2022, 3, 31));
        List<BigDecimal> avgCostList = new ArrayList<>();
        stockDetailList.forEach(stockDetail -> {
            BigDecimal avgCost = BigDecimal.valueOf((float) stockDetail.getTurnover() / stockDetail.getSharesTraded()).setScale(2, RoundingMode.HALF_UP);
            avgCostList.add(avgCost);
        });
        System.out.println(avgCostList);

        List<BigDecimal> avg5dCostList = countNdAvgCost(avgCostList, 5);
        System.out.println(avg5dCostList);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("1d   ");
        System.out.println(countNdAvgCost(avgCostList, 30));
        stopWatch.stop();
        System.out.println(stopWatch.getLastTaskName() + stopWatch.getLastTaskTimeNanos());

        stopWatch.start("5d   ");
        System.out.println(countNdAvgCostV2(avg5dCostList, 5, 30));
        stopWatch.stop();
        System.out.println(stopWatch.getLastTaskName() + stopWatch.getLastTaskTimeNanos());
    }

    public List<BigDecimal> countNdAvgCost(List<BigDecimal> inputAvgCostList, int outputN) {
        if (outputN > inputAvgCostList.size()
                || outputN <= 0
        ) return new ArrayList<>();
        List<BigDecimal> avgNdCostList = new ArrayList<>();

        BigDecimal sum = new BigDecimal(0);
        for (int i = 0; i < outputN; i++) {
            sum = sum.add(inputAvgCostList.get(i));
        }
        avgNdCostList.add(sum.divide(new BigDecimal(outputN), 2, RoundingMode.HALF_UP));
        for (int i = outputN; i < inputAvgCostList.size(); i++) {
            sum = sum.add(inputAvgCostList.get(i)).subtract(inputAvgCostList.get(i - outputN));
            avgNdCostList.add(sum.divide(new BigDecimal(outputN), 2, RoundingMode.HALF_UP));
        }
        return avgNdCostList;
    }

    public List<BigDecimal> countNdAvgCostV2(List<BigDecimal> inputAvgNdCostList, int inputN, int outputN) {
        if (outputN > inputAvgNdCostList.size()
                || outputN <= 0
                || outputN % inputN != 0
                || inputN > outputN
        ) return new ArrayList<>();
        List<BigDecimal> avgNdCostList = new ArrayList<>();

        int n = outputN / inputN;
        for (int j = 0; j < inputAvgNdCostList.size() + 1 - outputN; j++) {
            BigDecimal sum = new BigDecimal(0);
            try {
                for (int i = 0; i < n; i++) {
                    sum = sum.add(inputAvgNdCostList.get(j + inputN * i));
                }
                avgNdCostList.add(sum.divide(new BigDecimal(n), 2, RoundingMode.HALF_UP));
            } catch (Exception e) {
                break;
            }
        }
        return avgNdCostList;
    }

    @Test
    public void calculateStockStatistics() {
        stockStatisticsService.calculateStockStatistics("2330", LocalDate.of(2022, 2, 28), LocalDate.of(2022, 4, 1));
    }
}
