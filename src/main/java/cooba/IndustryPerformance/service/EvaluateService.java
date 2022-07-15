package cooba.IndustryPerformance.service;

import com.fasterxml.jackson.core.type.TypeReference;
import cooba.IndustryPerformance.database.entity.EvaluateEntity.EvaluateEntity;
import cooba.IndustryPerformance.database.repository.EvaluateEntityRepository;
import cooba.IndustryPerformance.entity.StockDetailStatistics;
import cooba.IndustryPerformance.entity.StockTopRankEntity;
import cooba.IndustryPerformance.utility.RedisCacheUtility;
import cooba.IndustryPerformance.utility.RedisUtility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static cooba.IndustryPerformance.constant.CommonConstant.YM;
import static cooba.IndustryPerformance.constant.RedisConstant.*;
import static cooba.IndustryPerformance.service.LocalcacheService.getlistedStockTimeToMarketLessThan1YearList;

@Slf4j
@Service
public class EvaluateService {
    @Autowired
    StockStatisticsService stockStatisticsService;
    @Autowired
    EvaluateEntityRepository evaluateEntityRepository;
    @Autowired
    TimeCounterService timeCounterService;
    @Autowired
    RedisCacheUtility redisCacheUtility;
    @Autowired
    RedisUtility redisUtility;

    private final int days = 10;
    private final int listlength = 100;

    public void evaluateMain(int year, int month) {
        List<EvaluateEntity> evaluateEntityList = redisCacheUtility.readEvaluateEntityMonthCache(year, month);
        List<EvaluateEntity> filterOutTimeToMarketLessThan1YearList = evaluateEntityList.stream()
                .filter(evaluateEntity -> !getlistedStockTimeToMarketLessThan1YearList().contains(evaluateEntity.getStockcode()))
                .collect(Collectors.toList());
        log.info("讀取 EvaluateEntity Cache: {}/{}", year, month);

        List<EvaluateEntity> MA5 = filterOutTimeToMarketLessThan1YearList.stream()
                .sorted((e1, e2) -> {
                    Double sum1 = e1.getMA5SlopeList().stream().mapToDouble(BigDecimal::doubleValue).sum();
                    Double sum2 = e2.getMA5SlopeList().stream().mapToDouble(BigDecimal::doubleValue).sum();
                    return sum2.compareTo(sum1);
                }).collect(Collectors.toList()).subList(0, listlength);
        Map<String, StockTopRankEntity> MA5Map = new ConcurrentHashMap<>();
        MA5.forEach(evaluateEntity -> {
            String stockcode = evaluateEntity.getStockcode();
            StockTopRankEntity stockTopRankEntity = StockTopRankEntity.convert(stockcode,
                    evaluateEntity.getMA5SlopeList(),
                    evaluateEntity,
                    redisCacheUtility.readStockBasicInfoCache(stockcode));
            MA5Map.put(stockcode, stockTopRankEntity);
        });
        redisUtility.valueObjectSet(MA5SLOPELIST + year + "_" + month, MA5Map, 40, TimeUnit.DAYS);
        log.info("寫入MA5 EvaluateEntity Cache: {}/{}", year, month);

        List<EvaluateEntity> MA10 = filterOutTimeToMarketLessThan1YearList.stream()
                .sorted((e1, e2) -> {
                    Double sum1 = e1.getMA10SlopeList().stream().mapToDouble(BigDecimal::doubleValue).sum();
                    Double sum2 = e2.getMA10SlopeList().stream().mapToDouble(BigDecimal::doubleValue).sum();
                    return sum2.compareTo(sum1);
                }).collect(Collectors.toList()).subList(0, listlength);
        Map<String, StockTopRankEntity> MA10Map = new ConcurrentHashMap<>();
        MA10.forEach(evaluateEntity -> {
            String stockcode = evaluateEntity.getStockcode();
            StockTopRankEntity stockTopRankEntity = StockTopRankEntity.convert(stockcode,
                    evaluateEntity.getMA10SlopeList(),
                    evaluateEntity,
                    redisCacheUtility.readStockBasicInfoCache(stockcode));
            MA10Map.put(stockcode, stockTopRankEntity);
        });
        redisUtility.valueObjectSet(MA10SLOPELIST + year + "_" + month, MA10Map, 40, TimeUnit.DAYS);
        log.info("寫入MA10 EvaluateEntity Cache: {}/{}", year, month);

        List<EvaluateEntity> MA21 = filterOutTimeToMarketLessThan1YearList.stream()
                .sorted((e1, e2) -> {
                    Double sum1 = e1.getMA21SlopeList().stream().mapToDouble(BigDecimal::doubleValue).sum();
                    Double sum2 = e2.getMA21SlopeList().stream().mapToDouble(BigDecimal::doubleValue).sum();
                    return sum2.compareTo(sum1);
                }).collect(Collectors.toList()).subList(0, listlength);
        Map<String, StockTopRankEntity> MA21Map = new ConcurrentHashMap<>();
        MA21.forEach(evaluateEntity -> {
            String stockcode = evaluateEntity.getStockcode();
            StockTopRankEntity stockTopRankEntity = StockTopRankEntity.convert(stockcode,
                    evaluateEntity.getMA21SlopeList(),
                    evaluateEntity,
                    redisCacheUtility.readStockBasicInfoCache(stockcode));
            MA21Map.put(stockcode, stockTopRankEntity);
        });
        redisUtility.valueObjectSet(MA21SLOPELIST + year + "_" + month, MA21Map, 40, TimeUnit.DAYS);
        log.info("寫入MA21 EvaluateEntity Cache: {}/{}", year, month);

        List<EvaluateEntity> MA62 = filterOutTimeToMarketLessThan1YearList.stream()
                .sorted((e1, e2) -> {
                    Double sum1 = e1.getMA62SlopeList().stream().mapToDouble(BigDecimal::doubleValue).sum();
                    Double sum2 = e2.getMA62SlopeList().stream().mapToDouble(BigDecimal::doubleValue).sum();
                    return sum2.compareTo(sum1);
                }).collect(Collectors.toList()).subList(0, listlength);
        Map<String, StockTopRankEntity> MA62Map = new ConcurrentHashMap<>();
        MA62.forEach(evaluateEntity -> {
            String stockcode = evaluateEntity.getStockcode();
            StockTopRankEntity stockTopRankEntity = StockTopRankEntity.convert(stockcode,
                    evaluateEntity.getMA62SlopeList(),
                    evaluateEntity,
                    redisCacheUtility.readStockBasicInfoCache(stockcode));
            MA62Map.put(stockcode, stockTopRankEntity);
        });
        redisUtility.valueObjectSet(MA62SLOPELIST + year + "_" + month, MA62Map, 40, TimeUnit.DAYS);
        log.info("寫入MA62 EvaluateEntity Cache: {}/{}", year, month);
    }

    @Async("stockExecutor")
    public void createEvaluateEntityAsync(String uuid, List<StockDetailStatistics> statisticsList, int year, int month, String stockcode) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        createEvaluateEntity(statisticsList, year, month, stockcode);
        stopWatch.stop();
        timeCounterService.addTime(uuid, stopWatch.getTotalTimeSeconds());
    }

    @Transactional
    public EvaluateEntity createEvaluateEntity(List<StockDetailStatistics> statisticsList, int year, int month, String stockcode) {
        if (statisticsList == null) {
            statisticsList = stockStatisticsService.getStockcodeStatisticsList(stockcode, year, month, 91);
        }
        if (statisticsList.isEmpty()) return new EvaluateEntity();

        EvaluateEntity evaluateEntity = EvaluateEntity.builder()
                .stockcode(stockcode)
                .year(year)
                .month(month)
                .dateStr(LocalDate.of(year, month, 1).format(YM))
                .build();
        simpleEvaluateMA(evaluateEntity, statisticsList, stockcode);

        List<BigDecimal> MA5SlopeList = evaluateMA(statisticsList.stream().map(StockDetailStatistics::get平均5日成本).collect(Collectors.toList()), stockcode);
        evaluateEntity.setMA5SlopeList(MA5SlopeList);
        List<BigDecimal> MA10SlopeList = evaluateMA(statisticsList.stream().map(StockDetailStatistics::get平均10日成本).collect(Collectors.toList()), stockcode);
        evaluateEntity.setMA10SlopeList(MA10SlopeList);
        List<BigDecimal> MA21SlopeList = evaluateMA(statisticsList.stream().map(StockDetailStatistics::get平均21日成本).collect(Collectors.toList()), stockcode);
        evaluateEntity.setMA21SlopeList(MA21SlopeList);
        List<BigDecimal> MA62SlopeList = evaluateMA(statisticsList.stream().map(StockDetailStatistics::get平均62日成本).collect(Collectors.toList()), stockcode);
        evaluateEntity.setMA62SlopeList(MA62SlopeList);

        List<BigDecimal> sdList = evaluateSD(statisticsList.stream().map(StockDetailStatistics::get平均股數).collect(Collectors.toList()));
        evaluateEntity.setAvgShareSDList(sdList);

        saveEvaluateEntity(evaluateEntity);
        return evaluateEntity;
    }

    public void saveEvaluateEntity(EvaluateEntity evaluateEntity) {
        String stockcode = evaluateEntity.getStockcode();
        int year = evaluateEntity.getYear();
        int month = evaluateEntity.getMonth();
        String dateStr = LocalDate.of(year, month, 1).format(YM);
        synchronized (LocalcacheService.getStockcodeLock(stockcode)) {
            evaluateEntityRepository.findByStockcodeAndDateStr(stockcode, dateStr).ifPresentOrElse(
                    (oldEvaluateEntity -> {
                        evaluateEntity.setId(oldEvaluateEntity.getId());
                        evaluateEntityRepository.save(evaluateEntity);
                    })
                    , () -> {
                        evaluateEntityRepository.save(evaluateEntity);
                    }
            );
        }

    }

    public void simpleEvaluateMA(EvaluateEntity evaluateEntity, List<StockDetailStatistics> statisticsList, String stockcode) {
        evaluateEntity.setMA5SlopeAbove0(statisticsList.get(0).get平均5日成本().compareTo(statisticsList.get(statisticsList.size() - 1).get平均5日成本()) < 0);
        evaluateEntity.setMA10SlopeAbove0(statisticsList.get(0).get平均10日成本().compareTo(statisticsList.get(statisticsList.size() - 1).get平均10日成本()) < 0);
        evaluateEntity.setMA21SlopeAbove0(statisticsList.get(0).get平均21日成本().compareTo(statisticsList.get(statisticsList.size() - 1).get平均21日成本()) < 0);
        evaluateEntity.setMA62SlopeAbove0(statisticsList.get(0).get平均62日成本().compareTo(statisticsList.get(statisticsList.size() - 1).get平均62日成本()) < 0);

        int times = statisticsList.size() / days;
        int MA5PositiveCount = 0;
        int MA10PositiveCount = 0;
        int MA21PositiveCount = 0;
        int MA62PositiveCount = 0;
        for (int i = 0; i < times - 1; i++) {
            int first = i * days;
            int second = first + days;
            if (second > statisticsList.size() - 1) {
                second = statisticsList.size() - 1;
            }
            MA5PositiveCount = statisticsList.get(first).get平均5日成本().compareTo(statisticsList.get(second).get平均5日成本()) < 0 ? MA5PositiveCount + 1 : MA5PositiveCount;
            MA10PositiveCount = statisticsList.get(first).get平均10日成本().compareTo(statisticsList.get(second).get平均10日成本()) < 0 ? MA10PositiveCount + 1 : MA10PositiveCount;
            MA21PositiveCount = statisticsList.get(first).get平均21日成本().compareTo(statisticsList.get(second).get平均21日成本()) < 0 ? MA21PositiveCount + 1 : MA21PositiveCount;
            MA62PositiveCount = statisticsList.get(first).get平均62日成本().compareTo(statisticsList.get(second).get平均62日成本()) < 0 ? MA62PositiveCount + 1 : MA62PositiveCount;
        }
        evaluateEntity.setMA5PositiveCount(MA5PositiveCount > times / 2);
        evaluateEntity.setMA10PositiveCount(MA10PositiveCount > times / 2);
        evaluateEntity.setMA21PositiveCount(MA21PositiveCount > times / 2);
        evaluateEntity.setMA62PositiveCount(MA62PositiveCount > times / 2);

        int MA5AboveMA10 = 0;
        int MA10AboveMA21 = 0;
        int MA21AboveMA62 = 0;
        for (StockDetailStatistics statistics : statisticsList) {
            MA5AboveMA10 = statistics.get平均5日成本().compareTo(statistics.get平均10日成本()) > 0 ? MA5AboveMA10 + 1 : MA5AboveMA10;
            MA10AboveMA21 = statistics.get平均10日成本().compareTo(statistics.get平均21日成本()) > 0 ? MA10AboveMA21 + 1 : MA10AboveMA21;
            MA21AboveMA62 = statistics.get平均21日成本().compareTo(statistics.get平均62日成本()) > 0 ? MA21AboveMA62 + 1 : MA21AboveMA62;
        }
        evaluateEntity.setMA5AboveMA10(MA5AboveMA10 > statisticsList.size() * 0.6);
        evaluateEntity.setMA10AboveMA21(MA10AboveMA21 > statisticsList.size() * 0.6);
        evaluateEntity.setMA21AboveMA62(MA21AboveMA62 > statisticsList.size() * 0.6);
        log.info("完成簡單評估平均成本線 股票: {}", stockcode);
    }

    public List<BigDecimal> evaluateMA(List<BigDecimal> bigDecimalList, String stockcode) {
        ListIterator<BigDecimal> iterator = bigDecimalList.listIterator();
        BigDecimal prev = null;
        boolean isPositive = true;
        Queue<BigDecimal> changingQuene = new LinkedList<>();
        Queue<Integer> counterQuene = new LinkedList<>();
        int counter = 0;
        changingQuene.add(bigDecimalList.get(0));

        while (iterator.hasNext()) {
            BigDecimal loc = iterator.next();
            if (prev != null) {
                if (isPositive != loc.compareTo(prev) > 0) {
                    counterQuene.add(counter);
                    counter = 1;
                    changingQuene.add(prev);
                    isPositive = !isPositive;
                } else {
                    counter = loc.compareTo(prev) == 0 ? counter : counter + 1;
                }
            }
            prev = loc;
        }
        changingQuene.add(bigDecimalList.get(bigDecimalList.size() - 1));
        counterQuene.add(counter);
        log.info("{} 計算轉折點的Quene: {}", stockcode, changingQuene);
        log.info("{} 計算轉折所需交易天數的Quene: {}", stockcode, counterQuene);

        BigDecimal first = changingQuene.poll();
        BigDecimal positiveResult = new BigDecimal(0);
        BigDecimal negativeResult = new BigDecimal(0);
        Integer positiveCounter = 0;
        Integer negativeCounter = 0;
        while (!changingQuene.isEmpty()) {
            BigDecimal second = changingQuene.poll();
            BigDecimal growth = second.subtract(first);
            if (growth.compareTo(new BigDecimal(0)) > 0) {
                positiveResult = positiveResult.add(growth);
                positiveCounter += counterQuene.poll();
            } else {
                negativeResult = negativeResult.add(growth);
                negativeCounter += counterQuene.poll();
            }
            first = second;
        }
        log.info("{} 漲幅總和: {}", stockcode, positiveResult);
        log.info("{} 漲幅天數總和: {}", stockcode, positiveCounter);

        log.info("{} 跌幅總和: {}", stockcode, negativeResult);
        log.info("{} 跌幅天數總和: {}", stockcode, negativeCounter);

        List<BigDecimal> resultList = new ArrayList<>();
        resultList.add(positiveCounter == 0 ? new BigDecimal(0) : positiveResult.divide(BigDecimal.valueOf(positiveCounter), 2, RoundingMode.HALF_UP));
        resultList.add(negativeCounter == 0 ? new BigDecimal(0) : negativeResult.divide(BigDecimal.valueOf(negativeCounter), 2, RoundingMode.HALF_UP));
        return resultList;
    }

    public List<BigDecimal> evaluateSD(List<BigDecimal> bigDecimalList) {
        BigDecimal avg = new BigDecimal(0);
        BigDecimal x2 = new BigDecimal(0);
        for (BigDecimal x : bigDecimalList) {
            avg = avg.add(x);
        }
        avg = avg.divide(new BigDecimal(bigDecimalList.size()), 2, RoundingMode.HALF_UP);
        for (BigDecimal x : bigDecimalList) {
            x2 = x2.add(x.subtract(avg).pow(2));
        }
        x2 = x2.divide(new BigDecimal(bigDecimalList.size()), 2, RoundingMode.HALF_UP);
        BigDecimal sd = BigDecimal.valueOf(Math.sqrt(x2.floatValue()));
        List<BigDecimal> resultList = new ArrayList<>();
        for (int i = -2; i < 3; i++) {
            resultList.add(avg.add(sd.multiply(new BigDecimal(i))).setScale(2, RoundingMode.HALF_UP));
        }
        resultList.add(sd.setScale(2, RoundingMode.HALF_UP));
        return resultList;
    }

    public EvaluateEntity readStockEvaluateEntity(String stockcode, String dateStr) {
        return evaluateEntityRepository.findByStockcodeAndDateStr(stockcode, dateStr).orElse(new EvaluateEntity());
    }

    public List<Map<String, StockTopRankEntity>> getEvaluateMainList(int year, int month) {
        Map<String, StockTopRankEntity> MA5Map = (Map<String, StockTopRankEntity>) redisUtility.valueObjectGet(MA5SLOPELIST + year + "_" + month, new TypeReference<Map<String, StockTopRankEntity>>() {
        });
        Map<String, StockTopRankEntity> MA10Map = (Map<String, StockTopRankEntity>) redisUtility.valueObjectGet(MA10SLOPELIST + year + "_" + month, new TypeReference<Map<String, StockTopRankEntity>>() {
        });
        Map<String, StockTopRankEntity> MA21Map = (Map<String, StockTopRankEntity>) redisUtility.valueObjectGet(MA21SLOPELIST + year + "_" + month, new TypeReference<Map<String, StockTopRankEntity>>() {
        });
        Map<String, StockTopRankEntity> MA62Map = (Map<String, StockTopRankEntity>) redisUtility.valueObjectGet(MA62SLOPELIST + year + "_" + month, new TypeReference<Map<String, StockTopRankEntity>>() {
        });
        List<Map<String, StockTopRankEntity>> resultList = new ArrayList<>();
        resultList.add(MA5Map);
        resultList.add(MA10Map);
        resultList.add(MA21Map);
        resultList.add(MA62Map);

        Set<String> MA5Set = MA5Map.keySet();
        Set<String> MA10Set = MA10Map.keySet();
        Set<String> MA21Set = MA21Map.keySet();
        Set<String> MA62Set = MA62Map.keySet();
        Set<String> resultSet = new HashSet<>(MA5Set);
        resultSet.retainAll(MA10Set);
        resultSet.retainAll(MA21Set);
        resultSet.retainAll(MA62Set);
        Map<String, StockTopRankEntity> intersectionMap = resultSet.stream()
                .collect(Collectors.toMap((stockcode -> stockcode),
                        (stockcode -> StockTopRankEntity.builder().stockcode(stockcode).build())));
        resultList.add(intersectionMap);
        return resultList;
    }
}
