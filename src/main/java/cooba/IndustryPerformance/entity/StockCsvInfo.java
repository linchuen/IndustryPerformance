package cooba.IndustryPerformance.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class StockCsvInfo {
    String stockcode;
    LocalDate date;
}
