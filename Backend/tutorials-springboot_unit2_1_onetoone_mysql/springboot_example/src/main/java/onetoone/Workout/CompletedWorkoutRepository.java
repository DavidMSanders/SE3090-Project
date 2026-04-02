package onetoone.Workout;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompletedWorkoutRepository extends JpaRepository<CompletedWorkout, Long> {
}
