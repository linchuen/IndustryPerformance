package cooba.IndustryPerformance.database.entity.EvaluateEntity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document
@CompoundIndex(name = "compound_sc_ds_idx", def = "{ stockcode: 1, dateStr: -1}")
public class EvaluateEntity {
    @Id
    private String id;
    @Indexed(name = "sc_idx")
    private String stockcode;
    private int year;
    private int month;
    @Indexed(name = "ds_idx", direction = IndexDirection.DESCENDING)
    private String dateStr;

    private boolean MA5SlopeAbove0;
    private boolean MA10SlopeAbove0;
    private boolean MA21SlopeAbove0;
    private boolean MA62SlopeAbove0;

    private boolean MA5PositiveCount;
    private boolean MA10PositiveCount;
    private boolean MA21PositiveCount;
    private boolean MA62PositiveCount;

    private boolean MA5AboveMA10;
    private boolean MA10AboveMA21;
    private boolean MA21AboveMA62;

    private List<BigDecimal> MA5SlopeList;
    private List<BigDecimal> MA10SlopeList;
    private List<BigDecimal> MA21SlopeList;
    private List<BigDecimal> MA62SlopeList;

    private List<BigDecimal> avgShareSDList;
}
