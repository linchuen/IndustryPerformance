package cooba.IndustryPerformance.service;

import cooba.IndustryPerformance.database.repository.BlackListReposiotry;
import cooba.IndustryPerformance.database.repository.StockDetailRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
public class LocalcacheService {
    @Autowired
    StockDetailRepository stockDetailRepository;
    @Autowired
    BlackListReposiotry blackListReposiotry;

    private Set<String> blacklist = new HashSet<>();

    public void init() {
        stockDetailRepository.findByCompanyType("興櫃")
                .forEach(stockDetail -> blacklist.add(stockDetail.getStockcode()));
        blackListReposiotry.findAll().forEach(blackList -> blacklist.add(blackList.getStockcode()));
    }

    public Set<String> getBlacklist() {
        return blacklist;
    }
}
