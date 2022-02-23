package cooba.IndustryPerformance.database.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@Document
public class BlackList {
    @Id
    private String id;
    private String stockcode;
}
