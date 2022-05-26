package cooba.IndustryPerformance.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class StatisticsInfo {
    String stockcode;
    String uuid;
    LocalDate startDate;
    LocalDate endDate;
}
