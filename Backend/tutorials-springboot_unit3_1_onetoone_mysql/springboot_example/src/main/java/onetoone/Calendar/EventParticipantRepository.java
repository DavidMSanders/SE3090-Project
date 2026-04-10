package onetoone.Calendar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface EventParticipantRepository extends JpaRepository<EventParticipant, Long>{
List<EventParticipant> findByEventRef(Long eventRef);
List<EventParticipant> findByAttendeeId(Long attendeeId);
boolean existsByEventRefAndAttendeeId(Long eventRef, Long attendeeId);

@Transactional
    void deleteByEventRef(Long eventRef);
}
