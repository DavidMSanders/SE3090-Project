package onetoone.Calendar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.List;

@Repository
public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {
    List<CalendarEvent> findByCreatorId(Long creatorId);
    List<CalendarEvent> findByCreatorIdAndScheduledDateGreaterThanEqual(Long creatorId, LocalDate scheduledDate);
    List<CalendarEvent> findByCreatorIdAndScheduledDateBetween(Long creatorId, LocalDate start, LocalDate end);
    List<CalendarEvent> findByCreatorIdAndScheduledDate(Long creatorId, LocalDate scheduledDate);
    List<CalendarEvent> findByRuleRef(Long ruleRef);
}
