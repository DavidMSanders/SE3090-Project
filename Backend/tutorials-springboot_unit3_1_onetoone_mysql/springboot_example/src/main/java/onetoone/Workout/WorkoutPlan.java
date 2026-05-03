package onetoone.Workout;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Workout plan entity
 * Matches spec:
 * - workoutPlanId
 * - trainerId
 * - planName
 * - planDescription
 * - createdAt
 */
@Entity
@Table(name = "workout_plans")
public class WorkoutPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long workoutPlanId;

    private Long trainerId;

    private String planName;

    @Column(columnDefinition = "TEXT")
    private String planDescription;

    private LocalDateTime createdAt = LocalDateTime.now();

    public WorkoutPlan() {
    }

    public Long getWorkoutPlanId() {
        return workoutPlanId;
    }

    public void setWorkoutPlanId(Long workoutPlanId) {
        this.workoutPlanId = workoutPlanId;
    }

    public Long getTrainerId() {
        return trainerId;
    }

    public void setTrainerId(Long trainerId) {
        this.trainerId = trainerId;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public String getPlanDescription() {
        return planDescription;
    }

    public void setPlanDescription(String planDescription) {
        this.planDescription = planDescription;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}