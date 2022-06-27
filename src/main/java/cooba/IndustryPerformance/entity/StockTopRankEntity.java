package cooba.IndustryPerformance.entity;

import cooba.IndustryPerformance.database.entity.EvaluateEntity.EvaluateEntity;
import cooba.IndustryPerformance.database.entity.StockBasicInfo.StockBasicInfo;
import lombok.*;
import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockTopRankEntity {
    private String stockcode;
    private BigDecimal ascend;
    private BigDecimal descend;
    private String name;
    private String industryType;
    private String companyType;
    private String desciption;
    private LocalDate timeToMarket;
    private LocalDate createdTime;

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

    public static StockTopRankEntity convert(String stockcode, List<BigDecimal> slopeList, EvaluateEntity evaluateEntity, StockBasicInfo stockBasicInfo) {
        StockTopRankEntity stockTopRankEntity = StockTopRankEntity.builder()
                .stockcode(stockcode)
                .ascend(slopeList.get(0))
                .descend(slopeList.get(1))
                .build();
        try {
            BeanUtils.copyProperties(stockTopRankEntity, evaluateEntity);
            BeanUtils.copyProperties(stockTopRankEntity, stockBasicInfo);
            return stockTopRankEntity;
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return stockTopRankEntity;
        }
    }
}
