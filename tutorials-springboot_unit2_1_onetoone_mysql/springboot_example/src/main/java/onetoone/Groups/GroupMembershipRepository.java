package onetoone.Groups;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * @author Matthew Smith
 */
@Repository
public interface GroupMembershipRepository extends JpaRepository<GroupMembership, Long> {
    List<GroupMembership> findByGroup_GroupId(Long groupId);

    List<GroupMembership> findByUser_Id(int userId);

    boolean existsByGroup_GroupIdAndUser_Id(Long groupId, int userId);
}
