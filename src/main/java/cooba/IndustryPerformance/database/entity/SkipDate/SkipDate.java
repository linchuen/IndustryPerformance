package cooba.IndustryPerformance.database.entity.SkipDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class SkipDate {
    @Id
    @GeneratedValue
    private int id;
    @Column(columnDefinition = "DATE")
    private LocalDate skipDate;

    public SkipDate(LocalDate skipDate) {
        this.skipDate = skipDate;
    }
}
