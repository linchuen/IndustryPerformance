package cooba.IndustryPerformance.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cooba.IndustryPerformance.service.IndustryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class IndustryController {

    @Autowired
    IndustryService industryService;

    @GetMapping("industry/stockInfo/{industryType}")
    public String getIndustryStockInfo(@PathVariable String industryType) {
        try {
            Map<String, String> map = industryService.getIndustryStockInfo(industryType);
            String json = new ObjectMapper().writeValueAsString(map);
            return json;
        } catch (JsonProcessingException e) {
            return "";
        }
    }

    @GetMapping("industry/growth/{industryType}")
    public Float getIndustryGrowth(String industryType) {
        return industryService.getIndustryGrowth(industryType).floatValue();
    }

    @GetMapping("industry/n_days_growth")
    public Float getIndustry_n_DaysGrowth(@RequestParam("Days") int days, @RequestParam("Type") String industryType) {
        return industryService.getIndustry_n_DaysGrowth(days, industryType).floatValue();
    }

    @DeleteMapping("industry")
    public void deleteAllIndustryInfo() {
        industryService.deleteAllIndustryInfo();
    }
}
