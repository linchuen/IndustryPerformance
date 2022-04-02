package cooba.IndustryPerformance.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cooba.IndustryPerformance.enums.UrlEnum;
import cooba.IndustryPerformance.service.IndustryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
public class IndustryController {

    @Autowired
    IndustryService industryService;

    @GetMapping("industry/all")
    public List getAllIndustry() {
        return industryService.getAllIndustry();
    }

    @GetMapping("industry/type")
    public List getAllIndustryType() {
        List list = Arrays.stream(UrlEnum.values()).map(urlEnum -> urlEnum.name()).collect(Collectors.toList());
        return list;
    }

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

    @GetMapping("industry/sub/{industryType}")
    public String getSubIndustryInfo(@PathVariable String industryType) {
        try {
            Set<String> set = industryService.getSubIndustryInfo(industryType);
            String json = new ObjectMapper().writeValueAsString(set);
            return json;
        } catch (JsonProcessingException e) {
            return "";
        }
    }

    @GetMapping("industry/growth/{industryType}")
    public Float getIndustryGrowth(@PathVariable String industryType) {
        return industryService.getIndustryGrowth(industryType).floatValue();
    }

    @GetMapping("industry/growth")
    public Float getIndustry_n_DaysGrowth(@RequestParam("Days") int days, @RequestParam("Type") String industryType) {
        return industryService.getIndustry_n_DaysGrowth(days, industryType).floatValue();
    }

    @DeleteMapping("industry")
    public void deleteAllIndustryInfo() {
        industryService.deleteAllIndustryInfo();
    }
}
