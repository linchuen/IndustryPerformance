package cooba.IndustryPerformance.service;

import cooba.IndustryPerformance.constant.UrlEnum;
import cooba.IndustryPerformance.database.entity.Industry.Industry;
import cooba.IndustryPerformance.database.entity.Industry.Stock;
import cooba.IndustryPerformance.database.entity.Industry.SubIndustry;
import cooba.IndustryPerformance.database.repository.IndustryRepository;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class CrawlerService {

    @Autowired
    IndustryRepository industryRepository;

    public void saveAllIndustry() {
        UrlEnum[] urlEnums = UrlEnum.values();
        for (UrlEnum urlEnum : urlEnums) {
            saveIndustry(urlEnum.name(), urlEnum.getUrl());
        }
    }

    public void saveIndustry(String industryType, String siteurl) {
        List<SubIndustry> subIndustryList = crawlIndustry(siteurl);
        Industry industry = Industry.builder()
                .industryName(industryType)
                .subIndustries(subIndustryList)
                .build();
        industryRepository.save(industry);
        log.info("{}成功存儲", industryType);
    }

    public List<SubIndustry> crawlIndustry(String siteurl) {
        List<SubIndustry> subIndustryList = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(siteurl).get();
            Elements elements = doc.getElementsByClass("industry-stream-item");

            for (Element subIndustry : elements) {
                Elements industryName = subIndustry.getElementsByClass("industry-stream-sub-industry-name");

                if (!industryName.isEmpty()) {
                    Elements stocks = subIndustry.getElementsByClass("industry-stream-company");
                    List<Stock> stockList = new ArrayList<>();

                    for (Element stock : stocks) {
                        String stockcode = stock.text().split(" ")[0];
                        String name = stock.text().split(" ")[1];
                        stockList.add(new Stock(stockcode, name));
                    }
                    subIndustryList.add(new SubIndustry(industryName.text(), stockList));
                }
            }
        } catch (IOException e) {
            log.warn("{}", e.getMessage());
        }
        return subIndustryList;
    }
}
