package onetoone.Calendar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface RecurrenceRuleRepository extends JpaRepository<RecurrenceRule, Long>{
    List<RecurrenceRule> findByOwnerId(Long ownerId);
}
