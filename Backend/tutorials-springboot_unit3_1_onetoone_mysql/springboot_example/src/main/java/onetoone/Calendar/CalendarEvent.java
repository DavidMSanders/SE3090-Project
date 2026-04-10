package onetoone.Calendar;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "cal_events")
public class CalendarEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long eventKey;

    @Column(name = "creator_id", nullable = false)
    private Long creatorId;

    @Column(name = "event_label", nullable = false)
    private String eventLabel;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    @Column(name = "open_time")
    private LocalTime openTime;

    @Column(name = "close_time")
    private LocalTime closeTime;

    @Column(name = "event_notes")
    private String eventNotes;

    @Column(name = "rule_ref")
    private Long ruleRef;

    public CalendarEvent(){

    }

    public CalendarEvent(Long creatorId, String eventLabel, LocalDate scheduledDate, String eventNotes){
        this.creatorId = creatorId;
        this.eventLabel = eventLabel;
        this.scheduledDate = scheduledDate;
        this.eventNotes = eventNotes;
    }

    public Long getEventKey(){
        return eventKey;
    }

    public void setEventKey(Long eventKey){
        this.eventKey = eventKey;
    }

    public Long getCreatorId(){
        return creatorId;
    }

    public void setCreatorId(Long creatorId){
        this.creatorId = creatorId;
    }

    public String getEventLabel() {
        return eventLabel;
    }

    public void setEventLabel(String eventLabel){
        this.eventLabel = eventLabel;
    }

    public LocalDate getScheduledDate(){
        return scheduledDate;
    }

    public void setScheduledDate(LocalDate scheduledDate){
        this.scheduledDate = scheduledDate;
    }

    public LocalTime getOpenTime() {
        return openTime;
    }

    public void setOpenTime(LocalTime openTime){
        this.openTime = openTime;
    }

    public LocalTime getCloseTime(){
        return closeTime;
    }

    public void setCloseTime(LocalTime closeTime){
        this.closeTime = closeTime;
    }

    public String getEventNotes(){
        return eventNotes;
    }

    public void setEventNotes(String eventNotes){
        this.eventNotes = eventNotes;
    }

    public Long getRuleRef(){
        return ruleRef;
    }

    public void setRuleRef(Long ruleRef){
        this.ruleRef = ruleRef;
    }

    @Override
    public String toString(){
        return "CalendarEvent{" + "eventKey=" + eventKey + ", creatorId=" + creatorId + ", eventLabel" + eventLabel + '\'' +
                ", scheduledDate=" + scheduledDate + '}';
    }
}
