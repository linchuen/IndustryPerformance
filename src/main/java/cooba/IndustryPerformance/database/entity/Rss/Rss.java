package cooba.IndustryPerformance.database.entity.Rss;

import lombok.*;

import javax.persistence.Id;
import java.time.LocalDateTime;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Rss {
    @Id
    private int id;
    private String title;
    private String link;
    private String description;
    private LocalDateTime publishedDate;
    private String categories;
}
