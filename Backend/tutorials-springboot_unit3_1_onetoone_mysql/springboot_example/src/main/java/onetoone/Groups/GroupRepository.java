package onetoone.Groups;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * @author Chris Lopez
 */
@Repository
public interface GroupRepository extends JpaRepository<Group, Integer> {
        List<Group> findByGroupType(String groupType);
    }


