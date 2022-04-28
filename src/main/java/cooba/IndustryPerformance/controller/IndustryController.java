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

import static cooba.IndustryPerformance.constant.StockConstant.LISTED;
import static cooba.IndustryPerformance.constant.StockConstant.LISTEDOTC;

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
            return e.getMessage();
        }
    }

    @GetMapping("industry/stockInfo/{industryType}/{subIndustryName}")
    public String getIndustryStockInfo(@PathVariable String industryType, @PathVariable String subIndustryName) {
        try {
            //子產業包含/會導致發送request失敗
            subIndustryName = subIndustryName.replace("->", "/");
            Map<String, String> map = industryService.getSubIndustryStockInfo(industryType, subIndustryName);
            String json = new ObjectMapper().writeValueAsString(map);
            return json;
        } catch (JsonProcessingException e) {
            return e.getMessage();
        }
    }

    @GetMapping("industry/subindustry/{industryType}")
    public String getSubIndustryInfo(@PathVariable String industryType) {
        try {
            Set<String> set = industryService.getSubIndustryInfo(industryType);
            String json = new ObjectMapper().writeValueAsString(set);
            return json;
        } catch (JsonProcessingException e) {
            return e.getMessage();
        }
    }

    @GetMapping("industry/growth/{industryType}")
    public Float getIndustryGrowth(@PathVariable String industryType) {
        return industryService.getIndustryGrowth(industryType).floatValue();
    }

    @GetMapping("industry/growth/{industryType}/{subIndustryName}")
    public Float getSubindustryGrowth(@PathVariable String industryType, @PathVariable String subIndustryName) {
        subIndustryName = subIndustryName.replace("->", "/");
        return industryService.getSubIndustryGrowth(industryType, subIndustryName).floatValue();
    }

    @GetMapping("industry/growth")
    public Float getIndustry_n_DaysGrowth(@RequestParam("Days") int days, @RequestParam("Type") String industryType) {
        return industryService.getIndustry_n_DaysGrowth(days, industryType, LISTEDOTC).floatValue();
    }

    @GetMapping("industry/growth/listed/")
    public Float getListedIndustry_n_DaysGrowth(@RequestParam("Days") int days, @RequestParam("Type") String industryType) {
        return industryService.getIndustry_n_DaysGrowth(days, industryType, LISTED).floatValue();
    }

    @GetMapping("subindustry/growth")
    public Float getSubindustry_n_DaysGrowth(@RequestParam("Days") int days, @RequestParam("Type") String industryType, @RequestParam("Subtype") String subIndustryName) {
        return industryService.getSubIndustry_n_DaysGrowth(days, industryType, subIndustryName, LISTEDOTC).floatValue();
    }

    @GetMapping("subindustry/growth/listed/")
    public Float getListedSubindustry_n_DaysGrowth(@RequestParam("Days") int days, @RequestParam("Type") String industryType, @RequestParam("Subtype") String subIndustryName) {
        return industryService.getSubIndustry_n_DaysGrowth(days, industryType, subIndustryName, LISTED).floatValue();
    }

    @DeleteMapping("industry")
    public void deleteAllIndustryInfo() {
        industryService.deleteAllIndustryInfo();
    }
}
