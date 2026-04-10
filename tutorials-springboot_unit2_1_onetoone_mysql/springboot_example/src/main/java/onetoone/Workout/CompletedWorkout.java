package onetoone.Workout;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * @author Matthew Smith
 */
@Entity
@Table(name = "completed_workouts")
public class CompletedWorkout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_workout_id", nullable = false)
    private AssignedWorkout assignedWorkout;

    @Column(nullable = false)
    private LocalDateTime dateCompleted;

    public CompletedWorkout() {
        this.dateCompleted = LocalDateTime.now();
    }

    public CompletedWorkout(AssignedWorkout assignedWorkout) {
        this.assignedWorkout = assignedWorkout;
        this.dateCompleted = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AssignedWorkout getAssignedWorkout() {
        return assignedWorkout;
    }

    public void setAssignedWorkout(AssignedWorkout assignedWorkout) {
        this.assignedWorkout = assignedWorkout;
    }

    public LocalDateTime getDateCompleted() {
        return dateCompleted;
    }

    public void setDateCompleted(LocalDateTime dateCompleted) {
        this.dateCompleted = dateCompleted;
    }
}
