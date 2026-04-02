package onetoone.Workout;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssignedWorkoutRepository extends JpaRepository<AssignedWorkout, Long> {
}
