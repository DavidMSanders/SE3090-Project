package __rk1.backend.service;

import __rk1.backend.model.CalendarEvent;
import __rk1.backend.repository.CalendarEventRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Matthew Smith
 */
@Service
public class CalendarEventService {

    private final CalendarEventRepository calendarEventRepository;

    public CalendarEventService(CalendarEventRepository calendarEventRepository) {
        this.calendarEventRepository = calendarEventRepository;
    }

    public CalendarEvent createEvent(Long userId, LocalDate eventDate, String workoutType, Integer durationMinutes,
            String notes) {
        CalendarEvent event = new CalendarEvent(userId, eventDate, workoutType, durationMinutes, notes);
        return calendarEventRepository.save(event);
    }

    public List<CalendarEvent> getEventsForDate(Long userId, LocalDate eventDate) {
        return calendarEventRepository.findByUserIdAndEventDate(userId, eventDate);
    }

    public List<CalendarEvent> getEventsForMonth(Long userId, int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        return calendarEventRepository.findByUserIdAndEventDateBetween(userId, start, end);
    }

    public List<LocalDate> getDaysWithEventsForMonth(Long userId, int year, int month) {
        return getEventsForMonth(userId, year, month).stream()
                .map(CalendarEvent::getEventDate)
                .distinct()
                .collect(Collectors.toList());
    }

    public CalendarEvent updateEvent(Long eventId, CalendarEvent updatedEvent) {
        CalendarEvent event = calendarEventRepository.findById(eventId).orElse(null);
        if (event != null) {
            if (updatedEvent.getWorkoutType() != null)
                event.setWorkoutType(updatedEvent.getWorkoutType());
            if (updatedEvent.getDurationMinutes() != null)
                event.setDurationMinutes(updatedEvent.getDurationMinutes());
            if (updatedEvent.getNotes() != null)
                event.setNotes(updatedEvent.getNotes());
            if (updatedEvent.getEventDate() != null)
                event.setEventDate(updatedEvent.getEventDate());
            return calendarEventRepository.save(event);
        }
        return null; // or throw exception depending on preference
    }

    public boolean deleteEvent(Long eventId) {
        if (calendarEventRepository.existsById(eventId)) {
            calendarEventRepository.deleteById(eventId);
            return true;
        }
        return false;
    }
}
