package cooba.IndustryPerformance.service.rssImpl;

import cooba.IndustryPerformance.enums.RssEnum;
import org.springframework.stereotype.Service;

@Service
public class Ettoday extends RssService {

    @Override
    public String getUrl() {
        return RssEnum.ettoday.getUrl();
    }
}