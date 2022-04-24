package cooba.IndustryPerformance.database.entity.SkipDate;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDate;

@Getter
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
