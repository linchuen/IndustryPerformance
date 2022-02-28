package cooba.IndustryPerformance.database.entity.StockDetail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document
public class StockDetail implements Serializable {
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
    private int tradingPiece;
    private LocalDate createdTime;
}
