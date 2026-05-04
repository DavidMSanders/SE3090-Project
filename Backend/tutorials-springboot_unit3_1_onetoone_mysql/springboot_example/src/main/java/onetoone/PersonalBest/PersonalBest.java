package onetoone.PersonalBest;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Stores personal best performance metrics for each user
 * Each entry is private and specific to each user
 */

@Entity
@Table(name = "personal_bests")
public class PersonalBest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long entryId;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "activity_label", nullable = false)
    private String activityLabel;

    @Column(name = "peak_score", nullable = false)
    private Double peakScore;

    @Column(name = "measure_unit")
    private String measureUnit;

    @Column(name = "achieved_date")
    private LocalDate achievedDate;

    public PersonalBest() {}

    public PersonalBest(Long ownerId, String activityLabel,
                        Double peakScore, String measureUnit,
                        LocalDate achievedDate) {
        this.ownerId = ownerId;
        this.activityLabel = activityLabel;
        this.peakScore = peakScore;
        this.measureUnit = measureUnit;
        this.achievedDate = achievedDate;
    }

    public Long getEntryId() { return entryId; }
    public void setEntryId(Long entryId) {
        this.entryId = entryId;
    }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getActivityLabel() { return activityLabel; }
    public void setActivityLabel(String activityLabel) {
        this.activityLabel = activityLabel;
    }

    public Double getPeakScore() { return peakScore; }
    public void setPeakScore(Double peakScore) {
        this.peakScore = peakScore;
    }

    public String getMeasureUnit() { return measureUnit; }
    public void setMeasureUnit(String measureUnit) {
        this.measureUnit = measureUnit;
    }

    public LocalDate getAchievedDate() { return achievedDate; }
    public void setAchievedDate(LocalDate achievedDate) {
        this.achievedDate = achievedDate;
    }

    @Override
    public String toString() {
        return "PersonalBest{" +
                "entryId=" + entryId +
                ", ownerId=" + ownerId +
                ", activityLabel='" + activityLabel + '\'' +
                ", peakScore=" + peakScore +
                ", measureUnit='" + measureUnit + '\'' +
                ", achievedDate=" + achievedDate +
                '}';
    }



}
