package onetoone.Calendar;
import jakarta.persistence.*;

@Entity
@Table(name = "event_participants")
public class EventParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long participantKey;

    @Column(name = "event_ref", nullable = false)
    private Long eventRef;

    @Column(name = "attendee_id", nullable = false)
    private Long attendeeId;

    @Column(name = "invite_status")
    private String inviteStatus = "INVITED";

    public EventParticipant(){

    }

    public EventParticipant(Long eventRef, Long attendeeId){
        this.eventRef = eventRef;
        this.attendeeId = attendeeId;
    }

    public Long getParticipantKey() {
        return participantKey;
    }
    public void setParticipantKey(Long participantKey) {
        this.participantKey = participantKey;
    }

    public Long getEventRef() {
        return eventRef;
    }
    public void setEventRef(Long eventRef) {
        this.eventRef = eventRef;
    }

    public Long getAttendeeId() {
        return attendeeId;
    }
    public void setAttendeeId(Long attendeeId) {
        this.attendeeId = attendeeId;
    }

    public String getInviteStatus() {
        return inviteStatus;
    }
    public void setInviteStatus(String inviteStatus) {
        this.inviteStatus = inviteStatus;
    }

    @Override
    public String toString() {
        return "EventParticipant{" +
                "participantKey=" + participantKey +
                ", eventRef=" + eventRef +
                ", attendeeId=" + attendeeId +
                ", inviteStatus='" + inviteStatus + '\'' +
                '}';
    }
}
