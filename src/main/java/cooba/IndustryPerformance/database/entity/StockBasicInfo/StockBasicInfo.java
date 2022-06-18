package cooba.IndustryPerformance.database.entity.StockBasicInfo;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Document
public class StockBasicInfo {
    @Id
    private String id;
    private String stockcode;
    private String name;
    private String industryType;
    private String companyType;
    private String desciption;
    private LocalDate timeToMarket;
    private LocalDate createdTime;
}