package onetoone.Calender;

import onetoone.Calender.CalendarEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * @author Matthew Smith
 */
@Repository
public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {

    List<CalendarEvent> findByUserIdAndEventDate(Long userId, LocalDate eventDate);

    List<CalendarEvent> findByUserIdAndEventDateBetween(Long userId, LocalDate start, LocalDate end);
}
