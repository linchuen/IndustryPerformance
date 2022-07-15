package cooba.IndustryPerformance.service.rssImpl;

import cooba.IndustryPerformance.enums.RssEnum;
import org.springframework.stereotype.Service;

@Service
public class Finance extends RssService {

    @Override
    public String getUrl() {
        return RssEnum.finance.getUrl();
    }

}
