package onetoone.Workout;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/*
Workout Repository INterface
@author Chris Lopez
 */

@Repository
public interface WorkoutRepository extends JpaRepository<Workout, Long> {
    // The JPA Repository uses the CRUD Operations
}
