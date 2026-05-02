package onetoone.Trainer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Matthew Smith
 */
@Repository
public interface TrainerClientRepository extends JpaRepository<TrainerClient, Long> {
    boolean existsByTrainerIdAndClientId(int trainerId, int clientId);
}
