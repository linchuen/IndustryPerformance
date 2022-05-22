package cooba.IndustryPerformance.database.entity.StockDetail;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Document
public class StockDetail {
    @Id
    private String id;
    private String stockcode;
    private String name;
    private String industryType;
    private String companyType;
    private BigDecimal price;
    private BigDecimal lastprice;
    private BigDecimal open;
    private BigDecimal highest;
    private BigDecimal lowest;
    private int tradingVolume;
    private LocalDate createdTime;
    private Long sharesTraded;
    private Long turnover;

    private String joinKey;
}
