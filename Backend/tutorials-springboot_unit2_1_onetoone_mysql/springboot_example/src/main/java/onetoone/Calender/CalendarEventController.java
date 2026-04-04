package onetoone.Calender;

import onetoone.Calender.CalendarEvent;
import onetoone.Calender.CalendarEventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * @author Matthew Smith
 */
@RestController
@RequestMapping("/api/calendar")
public class CalendarEventController {

    private final CalendarEventService calendarEventService;

    public CalendarEventController(CalendarEventService calendarEventService) {
        this.calendarEventService = calendarEventService;
    }

    @PostMapping
    public CalendarEvent createEvent(
            @RequestParam Long userId,
            @RequestParam String date,
            @RequestParam String workoutType,
            @RequestParam Integer durationMinutes,
            @RequestParam(required = false) String notes) {
        return calendarEventService.createEvent(
                userId,
                LocalDate.parse(date),
                workoutType,
                durationMinutes,
                notes);
    }

    @GetMapping("/date/{userId}/{date}")
    public List<CalendarEvent> getEventsByDate(
            @PathVariable Long userId,
            @PathVariable String date) {
        return calendarEventService.getEventsForDate(
                userId,
                LocalDate.parse(date));
    }

    @GetMapping("/months")
    public List<LocalDate> getEventsByMonth(
            @RequestParam Long userId,
            @RequestParam int year,
            @RequestParam int month) {
        return calendarEventService.getDaysWithEventsForMonth(userId, year, month);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CalendarEvent> updateEvent(@PathVariable Long id, @RequestBody CalendarEvent updatedEvent) {
        CalendarEvent event = calendarEventService.updateEvent(id, updatedEvent);
        if (event != null) {
            return ResponseEntity.ok(event);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        boolean deleted = calendarEventService.deleteEvent(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
