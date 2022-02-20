package cooba.IndustryPerformance.database.entity.StockDetail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@Document
public class StockDetail {
    @Id
    private String id;
    private String stockcode;
    private String name;
    private Float price;
    private Float lastprice;
    private Float open;
    private Float highest;
    private Float lowest;
    private int tradingVolume;
    private int tradingPiece;
    private LocalDate createdTime;
}
