package cooba.IndustryPerformance;

import cooba.IndustryPerformance.constant.RedisConstant;
import cooba.IndustryPerformance.database.entity.StockBasicInfo.StockBasicInfo;
import cooba.IndustryPerformance.utility.RedisUtility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class redisTest {
    @Autowired
    RedisUtility redisUtility;

    @Test
    public void Test() {
        StockBasicInfo stockBasicInfo = (StockBasicInfo) redisUtility.valueObjectGet(RedisConstant.STOCKBASICINFO + "2884", StockBasicInfo.class);
        System.out.println(stockBasicInfo);
    }
}
