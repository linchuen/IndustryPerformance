package cooba.IndustryPerformance.service;

import cooba.IndustryPerformance.database.entity.Industry.Stock;
import cooba.IndustryPerformance.database.entity.Industry.SubIndustry;
import cooba.IndustryPerformance.database.entity.StockDetail.StockDetail;
import cooba.IndustryPerformance.database.repository.IndustryRepository;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class CrawlerService {

    @Autowired
    IndustryRepository industryRepository;

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
            log.info("爬蟲 {} 成功", siteurl);
        } catch (IOException e) {
            log.warn("{}", e.getMessage());
        }
        return subIndustryList;
    }

    public StockDetail crawlStock(String stockcode) {
        String stockurl = String.format("https://goodinfo.tw/tw/StockDetail.asp?STOCK_ID=%s", stockcode);
        try {
            Document doc = Jsoup.connect(stockurl).get();
            Element table = doc.getElementsByClass("b1 p4_2 r10").get(0);
            String name = table.select("tbody > tr > td:nth-child(1) > nobr > a").text().split(" ")[1];
            String price = table.select("tbody > tr:nth-child(3) > td:nth-child(1)").text();
            String lastprice = table.select("tbody > tr:nth-child(3) > td:nth-child(2)").text();
            String open = table.select("tbody > tr:nth-child(3) > td:nth-child(6)").text();
            String highest = table.select("tbody > tr:nth-child(3) > td:nth-child(7)").text();
            String lowest = table.select("tbody > tr:nth-child(3) > td:nth-child(8)").text();
            String tradingVolume = table.select(" tbody > tr:nth-child(5) > td:nth-child(3)").text().replace(",", "");
            String tradingPiece = table.select(" tbody > tr:nth-child(5) > td:nth-child(1)").text().replace(",", "");

            StockDetail stock = StockDetail.builder()
                    .stockcode(stockcode)
                    .name(name)
                    .price(Float.parseFloat(price))
                    .lastprice(Float.parseFloat(lastprice))
                    .open(Float.parseFloat(open))
                    .highest(Float.parseFloat(highest))
                    .lowest(Float.parseFloat(lowest))
                    .tradingVolume(Integer.parseInt(tradingVolume))
                    .tradingPiece(Integer.parseInt(tradingPiece))
                    .createdTime(LocalDate.now())
                    .build();
            log.info("爬蟲 {} {} 成功", stockcode, name);
            return stock;
        } catch (Exception e) {
            log.warn("{}爬取失敗 ", stockcode);
            log.error(e.getMessage());
        }
        return null;
    }
}
