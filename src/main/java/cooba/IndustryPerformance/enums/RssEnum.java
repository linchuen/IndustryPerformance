package cooba.IndustryPerformance.enums;

public enum RssEnum {
    ltn("自由時報", "https://news.ltn.com.tw/rss/business.xml"),
    ettoday("ETtoday 財經新聞", "https://feeds.feedburner.com/ettoday/finance"),
    technews("科技新報", "https://technews.tw/tn-rss/"),
    finance("財經新報", "https://technews.tw/financerss/");

    private String name;
    private String url;

    RssEnum(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
