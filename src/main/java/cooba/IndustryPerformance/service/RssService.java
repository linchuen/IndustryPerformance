package cooba.IndustryPerformance.service;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import cooba.IndustryPerformance.enums.RssEnum;

import java.net.URL;

public class RssService {

    public void subscribe() {
    }

    public static void main(String[] args) {
        try {
            String url = RssEnum.taipeiecon.getUrl();
            try (XmlReader reader = new XmlReader(new URL(url))) {
                SyndFeed feed = new SyndFeedInput().build(reader);
                System.out.println(feed.getTitle());
                System.out.println("***********************************");
                for (SyndEntry entry : feed.getEntries()) {
                    System.out.println(entry);
                    System.out.println("***********************************");
                }
                System.out.println("Done");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
