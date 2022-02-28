package cooba.IndustryPerformance.service;

import cooba.IndustryPerformance.constant.RedisConstant;
import cooba.IndustryPerformance.database.repository.StockDetailRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Slf4j
@Service
public class LocalcacheService {
    @Autowired
    StockDetailRepository stockDetailRepository;
    @Autowired
    RedisTemplate redisTemplate;

    @PostConstruct
    public void init() {
        stockDetailRepository.findByCompanyType("興櫃")
                .forEach(stockDetail -> redisTemplate.opsForValue().set(RedisConstant.BLACKLIST + stockDetail.getStockcode(), stockDetail.getStockcode()));
    }

}
