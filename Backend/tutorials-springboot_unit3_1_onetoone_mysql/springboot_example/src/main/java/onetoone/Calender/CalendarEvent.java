package onetoone.Calender;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author Matthew Smith
 */
@Entity(name = "CalenderEvent")
@Table(name = "calendar_events")
public class CalendarEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private LocalDate eventDate;

    private String workoutType;

    private Integer durationMinutes;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private LocalDateTime createdAt;

    public CalendarEvent() {
        this.createdAt = LocalDateTime.now();
    }

    public CalendarEvent(Long userId, LocalDate eventDate, String workoutType, Integer durationMinutes, String notes) {
        this.userId = userId;
        this.eventDate = eventDate;
        this.workoutType = workoutType;
        this.durationMinutes = durationMinutes;
        this.notes = notes;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public String getWorkoutType() {
        return workoutType;
    }

    public void setWorkoutType(String workoutType) {
        this.workoutType = workoutType;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

