package cooba.IndustryPerformance;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cooba.IndustryPerformance.constant.RedisConstant;
import cooba.IndustryPerformance.database.entity.StockBasicInfo.StockBasicInfo;
import cooba.IndustryPerformance.database.entity.StockDetail.StockDetail;
import cooba.IndustryPerformance.database.entity.StockStatistics.StockStatistics;
import cooba.IndustryPerformance.utility.RedisCacheUtility;
import cooba.IndustryPerformance.utility.RedisUtility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

import static cooba.IndustryPerformance.constant.RedisConstant.STOCKDETAILLIST;

@SpringBootTest
public class redisTest {
    @Autowired
    RedisUtility redisUtility;
    @Autowired
    RedisCacheUtility redisCacheUtility;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    ObjectMapper objectMapper;

    @Test
    public void Test() {
        StockBasicInfo stockBasicInfo = (StockBasicInfo) redisUtility.valueObjectGet(RedisConstant.STOCKBASICINFO + "2884", StockBasicInfo.class);
        System.out.println(stockBasicInfo);
    }

    @Test
    public void list() throws JsonProcessingException {
        List<StockDetail> stockDetailList = (List<StockDetail>) redisUtility.valueObjectGet(STOCKDETAILLIST, new TypeReference<List<StockDetail>>() {
        });
        System.out.println(stockDetailList);
    }

    @Test
    public void readStockDetailMonthCache() {
        List<StockDetail> stockDetailList = redisCacheUtility.readStockDetailMonthCache("2330", 2022, 5);
        stockDetailList.forEach(System.out::println);
    }

    @Test
    public void readStockStatisticsMonthCache() {
        List<StockStatistics> stockStatisticsList = redisCacheUtility.readStockStatisticsMonthCache("2330", 2022, 5);
        stockStatisticsList.forEach(System.out::println);
    }
}
