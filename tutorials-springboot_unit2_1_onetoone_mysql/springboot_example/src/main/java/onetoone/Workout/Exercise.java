package onetoone.Workout;

import jakarta.persistence.*;

/**
 * @author Matthew Smith
 */
@Entity
@Table(name = "exercises")
public class Exercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long exerciseId;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String targetMuscleGroup;

    public Exercise() {
    }

    public Long getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(Long exerciseId) {
        this.exerciseId = exerciseId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTargetMuscleGroup() {
        return targetMuscleGroup;
    }

    public void setTargetMuscleGroup(String targetMuscleGroup) {
        this.targetMuscleGroup = targetMuscleGroup;
    }
}
