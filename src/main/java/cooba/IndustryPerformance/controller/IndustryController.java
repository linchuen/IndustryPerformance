package cooba.IndustryPerformance.controller;

import cooba.IndustryPerformance.service.IndustryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
public class IndustryController {

    @Autowired
    IndustryService industryService;

    @GetMapping("industry/stockInfo/{industryType}}")
    public Map<String, String> getIndustryStockInfo(@PathVariable String industryType) {
        return industryService.getIndustryStockInfo(industryType);
    }

    @GetMapping("industry/growth/{industryType}}")
    public BigDecimal getIndustryGrowth(String industryType) {
        return industryService.getIndustryGrowth(industryType);
    }

    @GetMapping("industry/n_days_growth")
    public BigDecimal getIndustry_n_DaysGrowth(@RequestParam("Days") int days, @RequestParam("Type") String industryType) {
        return industryService.getIndustry_n_DaysGrowth(days, industryType);
    }

    @DeleteMapping("industry")
    public void deleteAllIndustryInfo() {
        industryService.deleteAllIndustryInfo();
    }
}
