package cooba.IndustryPerformance.database.entity.StockStatistics;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Document
public class StockStatistics {
    @Id
    private String id;
    private String stockcode;
    private BigDecimal avgCost;
    private BigDecimal avg5dCost;
    private BigDecimal avg10dCost;
    private BigDecimal avg30dCost;
    private BigDecimal avg90dCost;
    private LocalDate tradingDate;
}
