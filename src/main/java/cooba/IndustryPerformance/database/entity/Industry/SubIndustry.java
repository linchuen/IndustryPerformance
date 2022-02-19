package cooba.IndustryPerformance.database.entity.Industry;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SubIndustry {
    private String subIndustryName;
    private List<Stock> companies;
}
