package cooba.IndustryPerformance.service;

import cooba.IndustryPerformance.constant.RedisConstant;
import cooba.IndustryPerformance.database.entity.Industry.Stock;
import cooba.IndustryPerformance.database.entity.Industry.SubIndustry;
import cooba.IndustryPerformance.database.entity.StockBasicInfo.StockBasicInfo;
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
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class CrawlerService {
    @Autowired
    RedisUtility redisUtility;

    private static final Integer WAITTIME = 2000;
    private static final String UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.127 Safari/537.36";

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
            log.warn("class:{} error:{}", getClass().getName(), e.getMessage());
        }
        return subIndustryList;
    }

    public StockDetail crawlStock(String stockcode) {
        if (redisUtility.hasKey(RedisConstant.BLACKLIST + stockcode)) {
            log.warn("crawlStock {} 在黑名單中", stockcode);
            return null;
        }
        Random random = new Random();
        int i = random.nextInt(2);
        StockDetail stockDetail;
        stockDetail = i == 1 ? crawlAnueSourceStock(stockcode) : crawlYahooSourceStock(stockcode);
        stockDetail = stockDetail == null ? i == 0 ? crawlYahooSourceStock(stockcode) : crawlAnueSourceStock(stockcode) : stockDetail;
        stockDetail = stockDetail == null ? crawlGoodInfoSourceStock(stockcode) : stockDetail;

        if (stockDetail == null) {
            log.warn("{}爬取失敗", stockcode);
            redisUtility.valueSet(RedisConstant.BLACKLIST + stockcode, stockcode, 1, TimeUnit.HOURS);
        }
        return stockDetail;
    }

    public StockDetail crawlGoodInfoSourceStock(String stockcode) {
        String stockurl = String.format("https://goodinfo.tw/tw/StockDetail.asp?STOCK_ID=%s", stockcode);
        try {
            Document doc = Jsoup.connect(stockurl)
                    .userAgent(UA)
                    .referrer("http://www.google.com")
                    .get();
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
            log.info("爬蟲 GoodInfo {} {} 成功", stockcode, name);
            Thread.sleep(WAITTIME);
            return stock;
        } catch (Exception e) {
            log.warn("class:{} error:{}", getClass().getName(), e.getMessage());
            return null;
        }
    }

    public StockDetail crawlYahooSourceStock(String stockcode) {
        try {
            StockBasicInfo stockBasicInfo = (StockBasicInfo) redisUtility.valueObjectGet(RedisConstant.STOCKBASICINFO + stockcode, StockBasicInfo.class);
            stockBasicInfo = stockBasicInfo == null ? crawlStockBasicInfo(stockcode) : stockBasicInfo;
            if (stockBasicInfo == null) {
                log.warn("{}股票不存在", stockcode);
                return null;
            }

            String stockurl = String.format("https://tw.stock.yahoo.com/quote/%s", stockcode);
            Document doc = Jsoup.connect(stockurl)
                    .userAgent(UA)
                    .referrer("http://www.google.com")
                    .get();
            Element table = doc.select("#qsp-overview-realtime-info > div:nth-child(2) > div > div > ul").get(0);
            String price = table.select("li:nth-child(1) > span:nth-child(2)").text();
            String lastprice = table.select("li:nth-child(7) > span:nth-child(2)").text();
            String open = table.select("li:nth-child(2) > span:nth-child(2)").text();
            String highest = table.select("li:nth-child(3) > span:nth-child(2)").text();
            String lowest = table.select("li:nth-child(4) > span:nth-child(2)").text();
            String tradingVolume = table.select(" li:nth-child(10) > span:nth-child(2)").text().replace(",", "");

            StockDetail stock = StockDetail.builder()
                    .stockcode(stockcode)
                    .name(stockBasicInfo.getName())
                    .industryType(stockBasicInfo.getIndustryType())
                    .companyType(stockBasicInfo.getCompanyType())
                    .price(new BigDecimal(price))
                    .lastprice(new BigDecimal(lastprice))
                    .open(new BigDecimal(open))
                    .highest(new BigDecimal(highest))
                    .lowest(new BigDecimal(lowest))
                    .tradingVolume(Integer.parseInt(tradingVolume))
                    .createdTime(LocalDate.now())
                    .build();
            log.info("爬蟲 Yahoo {} {} 成功", stockcode, stockBasicInfo.getName());
            Thread.sleep(WAITTIME);
            return stock;
        } catch (Exception e) {
            log.warn("class:{} error:{}", getClass().getName(), e.getMessage());
            return null;
        }
    }

    public StockDetail crawlAnueSourceStock(String stockcode) {
        try {
            StockBasicInfo stockBasicInfo = (StockBasicInfo) redisUtility.valueObjectGet(RedisConstant.STOCKBASICINFO + stockcode, StockBasicInfo.class);
            stockBasicInfo = stockBasicInfo == null ? crawlStockBasicInfo(stockcode) : stockBasicInfo;
            if (stockBasicInfo == null) {
                log.warn("{}股票不存在", stockcode);
                return null;
            }

            String stockurl = String.format("https://invest.cnyes.com/twstock/TWS/%s", stockcode);
            Document doc = Jsoup.connect(stockurl)
                    .userAgent(UA)
                    .referrer("http://www.google.com")
                    .get();
            String price = doc.selectXpath(String.format("//*[@id='_profile-TWS:%s:STOCK']/div[2]/div[2]/div/div[6]/div[2]", stockcode)).text();
            String lastprice = doc.selectXpath(String.format("//*[@id='_profile-TWS:%s:STOCK']/div[2]/div[2]/div/div[4]/div[2]", stockcode)).text();
            String open = doc.selectXpath(String.format("//*[@id='_profile-TWS:%s:STOCK']/div[2]/div[2]/div/div[5]/div[2]", stockcode)).text();
            String highest = doc.selectXpath(String.format("//*[@id='_profile-TWS:%s:STOCK']/div[2]/div[2]/div/div[2]/div[2]", stockcode)).text().split("- ")[1];
            String lowest = doc.selectXpath(String.format("//*[@id='_profile-TWS:%s:STOCK']/div[2]/div[2]/div/div[2]/div[2]", stockcode)).text().split("- ")[0];
            String tradingVolume = doc.selectXpath(String.format("//*[@id='_profile-TWS:%s:STOCK']/div[2]/div[2]/div/div[1]/div[2]", stockcode)).text().replaceAll("[, 張]", "");

            StockDetail stock = StockDetail.builder()
                    .stockcode(stockcode)
                    .name(stockBasicInfo.getName())
                    .industryType(stockBasicInfo.getIndustryType())
                    .companyType(stockBasicInfo.getCompanyType())
                    .price(new BigDecimal(Float.valueOf(price)))
                    .lastprice(new BigDecimal(Float.valueOf(lastprice)))
                    .open(new BigDecimal(Float.valueOf(open)))
                    .highest(new BigDecimal(Float.valueOf(highest)))
                    .lowest(new BigDecimal(Float.valueOf(lowest)))
                    .tradingVolume(Integer.parseInt(tradingVolume))
                    .createdTime(LocalDate.now())
                    .build();
            log.info("爬蟲 Anue {} {} 成功", stockcode, stockBasicInfo.getName());
            Thread.sleep(WAITTIME);
            return stock;
        } catch (Exception e) {
            log.warn("class:{} error:{}", getClass().getName(), e.getMessage());
            return null;
        }
    }

    public StockBasicInfo crawlStockBasicInfo(String stockcode) {
        StockBasicInfo stockBasicInfo = crawlYahooStockBasicInfo(stockcode);
        stockBasicInfo = stockBasicInfo == null ? crawlGoodInfoStockBasicInfo(stockcode) : stockBasicInfo;
        return stockBasicInfo;
    }

    public StockBasicInfo crawlYahooStockBasicInfo(String stockcode) {
        try {
            String infostockurl = String.format("https://tw.stock.yahoo.com/quote/%s/profile", stockcode);
            Document infodoc = Jsoup.connect(infostockurl)
                    .userAgent(UA)
                    .referrer("http://www.google.com")
                    .get();
            String companyType = infodoc.selectXpath("//*[@id='main-2-QuoteProfile-Proxy']/div/section[1]/div[2]/div[20]/div/div").text();
            String industryType = infodoc.selectXpath("//*[@id='main-2-QuoteProfile-Proxy']/div/section[1]/div[2]/div[9]/div/div").text();
            String name = infodoc.selectXpath("//*[@id='main-2-QuoteProfile-Proxy']/div/section[1]/div[2]/div[1]/div/div").text();
            String desciption = infodoc.selectXpath("//*[@id='main-2-QuoteProfile-Proxy']/div/section[1]/div[3]/div").text();

            StockBasicInfo stockBasicInfo = StockBasicInfo.builder()
                    .stockcode(stockcode)
                    .name(name)
                    .industryType(industryType)
                    .companyType(companyType)
                    .desciption(desciption)
                    .createdTime(LocalDate.now())
                    .build();

            redisUtility.valueObjectSet(RedisConstant.STOCKBASICINFO + stockcode, stockBasicInfo);
            log.info("爬蟲 Yahoo {} {} 基本資料成功", stockcode, name);
            return stockBasicInfo;
        } catch (Exception e) {
            log.warn("爬蟲 Yahoo {} 基本資料失敗 class:{} error:{}", stockcode, getClass().getName(), e.getMessage());
            return null;
        }
    }

    public StockBasicInfo crawlGoodInfoStockBasicInfo(String stockcode) {
        String stockurl = String.format("https://goodinfo.tw/tw/StockDetail.asp?STOCK_ID=%s", stockcode);
        try {
            Document doc = Jsoup.connect(stockurl)
                    .userAgent(UA)
                    .referrer("http://www.google.com")
                    .get();
            Element infotable = doc.getElementsByClass("b1 p4_4 r10").get(0);
            String industryType = infotable.select("tbody > tr:nth-child(3) > td:nth-child(2)").text();
            String companyType = infotable.select(" tbody > tr:nth-child(4) > td:nth-child(2) > nobr").text();
            String desciption = infotable.select(" tbody > tr:nth-child(15) > td > p").text();
            Element table = doc.getElementsByClass("b1 p4_2 r10").get(0);
            String name = "";
            try {
                name = table.select("tbody > tr > td:nth-child(1) > nobr > a").text().split(" ")[1];
            } catch (ArrayIndexOutOfBoundsException arrayIndexOutOfBoundsException) {
                name = table.select("tbody > tr > th > span:nth-child(1) > a").text().split(" ")[1];
            }

            StockBasicInfo stockBasicInfo = StockBasicInfo.builder()
                    .stockcode(stockcode)
                    .name(name)
                    .industryType(industryType)
                    .companyType(companyType)
                    .desciption(desciption)
                    .createdTime(LocalDate.now())
                    .build();

            redisUtility.valueObjectSet(RedisConstant.STOCKBASICINFO + stockcode, stockBasicInfo);
            log.info("爬蟲 GoodInfo {} {} 基本資料成功", stockcode, name);
            return stockBasicInfo;
        } catch (Exception e) {
            log.warn("爬蟲 GoodInfo {} 基本資料失敗 class:{} error:{}", stockcode, getClass().getName(), e.getMessage());
            return null;
        }
    }
}
