package cooba.IndustryPerformance.database.entity.TimeCounter;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String uuid;
    private String method;
    private String message;
    private Double totalSeconds;
    private String comment;
    private LocalDateTime createTime;
}
