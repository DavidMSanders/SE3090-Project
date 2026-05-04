package onetoone.PersonalBest;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author Chris Lopez
 */
public interface PersonalBestRepository extends JpaRepository<PersonalBest, Long> {


    List<PersonalBest> findByOwnerId(Long ownerId);

    List<PersonalBest> findByOwnerIdAndActivityLabelContainingIgnoreCase(Long ownerId, String activityLabel);

}
