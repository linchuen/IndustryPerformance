package cooba.IndustryPerformance.database.entity.StockDetail;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import cooba.IndustryPerformance.database.entity.StockStatistics.StockStatistics;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate createdTime;
    private Long sharesTraded;
    private Long turnover;

    private String joinKey;
    private List<StockStatistics> stockStatistics;
}
