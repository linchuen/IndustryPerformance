package cooba.IndustryPerformance.controller;

import cooba.IndustryPerformance.service.SkipDateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SkipDateController {
    @Autowired
    SkipDateService skipDateService;

    @GetMapping("skipdate")
    public boolean updateSkipdate() {
        skipDateService.downloadSkipDateCsv();
        return skipDateService.createSkipDate();

    }
}
