package cooba.IndustryPerformance.database.entity.Industry;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SubIndustry {
    private String subIndustryName;
    @JsonIgnore
    private List<Stock> companies;
}
