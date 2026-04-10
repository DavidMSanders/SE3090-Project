package onetoone.Groups;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Matthew Smith
 */
@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
}
