package cooba.IndustryPerformance.database.entity.TimeCounter;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class TimeCounter {
    @Id
    @GeneratedValue
    private int id;
    private String uuid;
    private String method;
    private String message;
    private Double totalSeconds;
    private LocalDateTime createTime;
    private String comment;
}
