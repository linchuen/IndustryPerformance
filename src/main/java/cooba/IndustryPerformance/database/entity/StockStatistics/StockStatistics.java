package cooba.IndustryPerformance.database.entity.StockStatistics;


import cooba.IndustryPerformance.database.entity.StockDetail.StockDetail;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Document
@CompoundIndex(name = "compound_sc_td_idx", def = "{ stockcode: 1, tradingDate: -1}")
public class StockStatistics {
    @Id
    private String id;
    @Indexed(name = "sc_idx")
    private String stockcode;
    private BigDecimal avgCost;
    private BigDecimal avg5dCost;
    private BigDecimal avg10dCost;
    private BigDecimal avg21dCost;
    private BigDecimal avg62dCost;
    private BigDecimal avgShare;
    private BigDecimal avg10dVolume;
    private BigDecimal avg21dVolume;
    @Indexed(name = "td_idx", direction = IndexDirection.DESCENDING)
    private LocalDate tradingDate;

    private String joinKey;
    private List<StockDetail> stockDetail;
}
