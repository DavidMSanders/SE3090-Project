package onetoone.Groups;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * GroupMembership Repository
 * @author Chris Lopez
 */
public interface GroupMembershipRepository extends JpaRepository<GroupMembership, Integer> {
    List<GroupMembership> findByGroupId(int groupId);
    List<GroupMembership> findByUserId(int userId);
    Optional<GroupMembership> findByGroupIdAndUserId(int groupId, int userId);
    boolean existsByGroupIdAndUserId(int groupId, int userId);
}
