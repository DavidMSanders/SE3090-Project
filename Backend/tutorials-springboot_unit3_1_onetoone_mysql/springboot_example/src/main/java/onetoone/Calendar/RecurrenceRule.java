package onetoone.Calendar;

import jakarta.persistence.*;

@Entity
@Table(name = "recurrence_rules")
public class RecurrenceRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ruleKey;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "rule_label", nullable = false)
    private String ruleLabel;

    @Column(name = "rule_notes")
    private String ruleNotes;

    @Column(name = "repeat_day", nullable = false)
    private String repeatDay;

    @Column(name = "open_time")
    private String openTime;

    @Column(name = "close_time")
    private String closeTime;

    public RecurrenceRule() {
    }

    public Long getRuleKey(){
        return ruleKey;
    }

    public void setRuleKey(Long ruleKey){
        this.ruleKey = ruleKey;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId){
        this.ownerId = ownerId;
    }

    public String getRuleLabel(){
        return ruleLabel;
    }

    public void setRuleLabel(String ruleLabel){
        this.ruleLabel = ruleLabel;
    }

    public String getRuleNotes(){
        return ruleNotes;
    }

    public void setRuleNotes(String ruleNotes){
        this.ruleNotes = ruleNotes;
    }

    public String getRepeatDay(){
        return repeatDay;
    }

    public void setRepeatDay(String repeatDay){
        this.repeatDay = repeatDay;
    }

    public String getOpenTime(){
        return openTime;
    }

    public void setOpenTime(String openTime){
        this.openTime = openTime;
    }

    public String getCloseTime(){
        return closeTime;
    }

    public void setCloseTime(String closeTime){
        this.closeTime = closeTime;
    }

    @Override
    public String toString(){
        return "RecurrenceRule{" + "ruleKey=" + ruleKey + ", ownerId" + ownerId + ", ruleLabel=" + ruleLabel + '\'' +
                ", repeatDay=" + repeatDay + '\'' + '}';
    }
}
