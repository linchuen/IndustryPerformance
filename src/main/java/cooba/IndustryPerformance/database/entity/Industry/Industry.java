package cooba.IndustryPerformance.database.entity.Industry;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Document
public class Industry {
    @Id
    private String id;
    private List<SubIndustry> subIndustries;
    private String industryName;
    @JsonIgnore
    private LocalDateTime updatedTime;
}