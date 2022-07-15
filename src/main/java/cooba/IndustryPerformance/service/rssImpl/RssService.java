package cooba.IndustryPerformance.service.rssImpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rometools.rome.feed.synd.SyndCategory;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import cooba.IndustryPerformance.database.entity.Rss.Rss;
import cooba.IndustryPerformance.database.mapper.RssMapper;
import cooba.IndustryPerformance.enums.RssEnum;
import cooba.IndustryPerformance.service.WordAnalyzeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RssService {
    @Autowired
    RssMapper rssMapper;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    WordAnalyzeService wordAnalyzeService;

    public String getUrl() {
        return "";
    }

    public void subscribe(String url) {
        try {
            XmlReader reader = new XmlReader(new URL(url));
            SyndFeed feed = new SyndFeedInput().build(reader);
            List<Rss> rssList = rssMapper.findByPublishedDateGreaterThan(LocalDate.now().minusDays(1).atTime(0, 0, 0));
            for (SyndEntry entry : feed.getEntries()) {
                Rss rss = Rss.builder()
                        .title(entry.getTitle())
                        .link(entry.getLink())
                        .description(entry.getDescription().getValue())
                        .publishedDate(entry.getPublishedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                        .categories(objectMapper.writeValueAsString(entry.getCategories().stream().map(SyndCategory::getName).collect(Collectors.toList())))
                        .build();
                Optional<Rss> optionalRss = rssList.stream().filter(dbRss ->
                        !dbRss.getPublishedDate().equals(rss.getPublishedDate()) || !dbRss.getTitle().equals(rss.getTitle())
                ).findFirst();
                if (optionalRss.isEmpty()) {
                    rssMapper.insertRss(rss);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Map<String, Integer> processAnaylze() {
        Map<String, Integer> keywordMap = new HashMap<>();
        List<Rss> rssList = rssMapper.findByPublishedDateGreaterThan(LocalDate.now().minusDays(10).atTime(0, 0, 0));
        rssList.forEach(rss -> {
            wordAnalyzeService.wordAnalysis(keywordMap, rss.getTitle());
            wordAnalyzeService.wordAnalysis(keywordMap, rss.getDescription());
        });
        Map<String, Integer> resultMap = keywordMap.entrySet()
                .stream()
                .filter(words -> words.getKey().split(" ")[0].length() > 1)
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .limit(20)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        return resultMap;
    }

    public static void main(String[] args) throws JsonProcessingException {
        try {
            String url = RssEnum.technews.getUrl();
            try (XmlReader reader = new XmlReader(new URL(url))) {
                SyndFeed feed = new SyndFeedInput().build(reader);
                System.out.println(feed.getTitle());
                System.out.println("***********************************");
                for (SyndEntry entry : feed.getEntries()) {
                    System.out.println(entry.getTitle());
                    System.out.println(entry.getLink());
                    System.out.println(entry.getDescription().getValue());
                    System.out.println(entry.getPublishedDate());
                    System.out.println(entry.getCategories());
                    System.out.println("***********************************");
                }
                System.out.println("Done");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
