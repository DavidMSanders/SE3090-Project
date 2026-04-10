package onetoone.Workout;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * @author Matthew Smith
 */
@Repository
public interface WorkoutPlanRepository extends JpaRepository<WorkoutPlan, Long> {
    List<WorkoutPlan> findByTrainerId(Long trainerId);

    List<WorkoutPlan> findByGroupId(Long groupId);
}
