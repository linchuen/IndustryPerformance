package cooba.IndustryPerformance.database.entity.StockDetail;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Document
@CompoundIndex(name = "compound_sc_ct_idx", def = "{ stockcode: 1, createdTime: -1 }")
public class StockDetail {
    @Id
    private String id;
    @Indexed(name = "sc_idx")
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
    @Indexed(name = "ct_idx", direction = IndexDirection.DESCENDING)
    private LocalDate createdTime;
    private Long sharesTraded;
    private Long turnover;

    private String joinKey;
}
