package onetoone.Workout;

import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/*
Workout Repository INterface
@author Chris Lopez
 */

@Repository
public interface WorkoutRepository extends JpaRepository<Workout, Integer>{
}
