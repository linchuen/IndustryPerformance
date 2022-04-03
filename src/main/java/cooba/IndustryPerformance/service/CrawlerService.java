package cooba.IndustryPerformance.service;

import cooba.IndustryPerformance.constant.RedisConstant;
import cooba.IndustryPerformance.database.entity.Industry.Stock;
import cooba.IndustryPerformance.database.entity.Industry.SubIndustry;
import cooba.IndustryPerformance.database.entity.StockDetail.StockDetail;
import cooba.IndustryPerformance.utility.RedisUtility;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class CrawlerService {
    @Autowired
    RedisUtility redisUtility;

    private static Boolean PRIAMRYSOURCE = true;

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
        if (PRIAMRYSOURCE) {
            return crawlPriamrySourceStock(stockcode);
        } else {
            return crawlSecondarySourceStock(stockcode);
        }
    }

    public StockDetail crawlPriamrySourceStock(String stockcode) {
        String stockurl = String.format("https://goodinfo.tw/tw/StockDetail.asp?STOCK_ID=%s", stockcode);
        try {
            Document doc = Jsoup.connect(stockurl).get();
            Element infotable = doc.getElementsByClass("b1 p4_4 r10").get(0);
            String industryType = infotable.select("tbody > tr:nth-child(3) > td:nth-child(2)").text();
            String companyType = infotable.select(" tbody > tr:nth-child(4) > td:nth-child(2) > nobr").text();
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
                    .industryType(industryType)
                    .companyType(companyType)
                    .price(new BigDecimal(price))
                    .lastprice(new BigDecimal(lastprice))
                    .open(new BigDecimal(open))
                    .highest(new BigDecimal(highest))
                    .lowest(new BigDecimal(lowest))
                    .tradingVolume(Integer.parseInt(tradingVolume))
                    .tradingPiece(Integer.parseInt(tradingPiece))
                    .createdTime(LocalDate.now())
                    .build();
            log.info("爬蟲 {} {} 成功", stockcode, name);
            return stock;
        } catch (Exception e) {
            log.warn("{}爬取失敗", stockcode);
            redisUtility.valueSet(RedisConstant.BLACKLIST + stockcode, stockcode, 3, TimeUnit.DAYS);
            return null;
        }
    }

    public StockDetail crawlSecondarySourceStock(String stockcode) {
        try {
            String infostockurl = String.format("https://tw.stock.yahoo.com/quote/%s/profile", stockcode);
            Document infodoc = Jsoup.connect(infostockurl).get();
            String industryType = infodoc.select("#main-2-QuoteProfile-Proxy > div > section:nth-child(1) > div.table-grid.row-fit-half > div:nth-child(9) > div > div").text();
            String companyType = infodoc.select(" #main-2-QuoteProfile-Proxy > div > section:nth-child(1) > div.table-grid.row-fit-half > div:nth-child(20) > div > div").text();
            String name = infodoc.select("#main-2-QuoteProfile-Proxy > div > section:nth-child(1) > div.table-grid.row-fit-half > div:nth-child(1) > div > div").text();
            String stockurl = String.format("https://tw.stock.yahoo.com/quote/%s.TW", stockcode);
            Document doc = Jsoup.connect(stockurl).get();
            Element table = doc.select("#qsp-overview-realtime-info > div:nth-child(2) > div > div > ul").get(0);
            String price = table.select("li:nth-child(1) > span:nth-child(2)").text();
            String lastprice = table.select("li:nth-child(7) > span:nth-child(2)").text();
            String open = table.select("li:nth-child(2) > span:nth-child(2)").text();
            String highest = table.select("li:nth-child(3) > span:nth-child(2)").text();
            String lowest = table.select("li:nth-child(4) > span:nth-child(2)").text();
            String tradingVolume = table.select(" li:nth-child(10) > span:nth-child(2)").text().replace(",", "");

            StockDetail stock = StockDetail.builder()
                    .stockcode(stockcode)
                    .name(name)
                    .industryType(industryType)
                    .companyType(companyType)
                    .price(new BigDecimal(price))
                    .lastprice(new BigDecimal(lastprice))
                    .open(new BigDecimal(open))
                    .highest(new BigDecimal(highest))
                    .lowest(new BigDecimal(lowest))
                    .tradingVolume(Integer.parseInt(tradingVolume))
                    .createdTime(LocalDate.now())
                    .build();
            log.info("爬蟲 {} {} 成功", stockcode, name);
            return stock;
        } catch (Exception e) {
            log.warn("{}爬取失敗", stockcode);
            redisUtility.valueSet(RedisConstant.BLACKLIST + stockcode, stockcode, 3, TimeUnit.DAYS);
            return null;
        }
    }

    public void changeSource() {
        PRIAMRYSOURCE = !PRIAMRYSOURCE;
        log.info("爬蟲訊號源改變 {}", PRIAMRYSOURCE ? "goodinfo.tw" : "tw.stock.yahoo.com");
    }

    public static void main(String[] args) {
        CrawlerService crawlerService = new CrawlerService();
        System.out.println(crawlerService.crawlSecondarySourceStock("2330"));
    }
}
