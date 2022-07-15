package cooba.IndustryPerformance.controller;

import cooba.IndustryPerformance.service.rssImpl.RssService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class RssController {
    @Autowired
    RssService rssService;

    @Autowired
    List<? extends RssService> rssServices;

    @GetMapping("rss")
    public Map<String, Integer> createAllEvaluateEntity() {
        return rssService.processAnaylze();
    }

    @PostMapping("rss")
    public void news() {
        rssServices.forEach(rssService -> rssService.subscribe(rssService.getUrl()));
    }
}
