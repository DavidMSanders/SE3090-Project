package onetoone.Chat_Messages;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

//import lombok.Data;

/**
 *  @author Chris Lopez
 */

@Entity
@Table(name = "messages")
//@Data
public class Message {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (name = "sender_id", nullable = false)
    private Integer originatorId;

    @Column(name = "group_id")
    private Integer groupRef;


    @Column(name = "recipient_id", nullable = true)
    private Integer targetUserId;

    @Lob
    @Column(name = "message_text", nullable = false)
    private String textBody;

    @Column(name = "timeStamp", nullable = false)
    private LocalDateTime createdTimestamp = LocalDateTime.now();

    @Column(name = "is_deleted")
    private Boolean activeFlag = true;

    @Column(name = "is_read")
    private Boolean readFlag = false;

    @Column(name = "message_type")
    private String msgCategory = "NORMAL";

    @Column(name = "edited_at")
    private LocalDateTime editTimestamp;


    public Message(){
    }

    public Message(Integer originatorId, Integer targetUserId, String textBody){
        this.originatorId = originatorId;
        this.targetUserId = targetUserId;
        this.textBody = textBody;
    }

    public Long getId(){

        return id;
    }

    public void setId(Long id){

        this.id = id;
    }

    public Integer getOriginatorId(){

        return originatorId;
    }

    public void setOriginatorId(Integer originatorId){

        this.originatorId = originatorId;
    }

    public Integer getTargetUserId(){

        return targetUserId;
    }

    public void setTargetUserId(Integer targetUserId){

        this.targetUserId = targetUserId;
    }

    public String getTextBody(){
        return textBody;
    }

    public void setTextBody(String textBody){

        this.textBody = textBody;
    }

    public LocalDateTime getCreatedTimestamp(){

        return createdTimestamp;
    }

    public void setCreatedTimestamp(LocalDateTime createdTimestamp){

        this.createdTimestamp = createdTimestamp;
    }

    public Boolean getActiveFlag(){

        return activeFlag;
    }

    public void setActiveFlag(Boolean activeFlag){

        this.activeFlag = activeFlag;
    }

    public Boolean getReadFlag(){

        return readFlag;
    }

    public void setReadFlag(Boolean readFlag){

        this.readFlag = readFlag;
    }

    public LocalDateTime getEditTimestamp(){

        return editTimestamp;
    }
    public void setEditTimestamp(LocalDateTime editTimestamp){

        this.editTimestamp = editTimestamp;
    }

    @Override
    public String toString(){
        return "Message{" + "id=" + id + ", originatorId=" + originatorId +  ", targetUserId=" + targetUserId +
                ", createdTimeStamp" + createdTimestamp + ", readFlag=" + readFlag +
                '}';
    }
    /*@Temporal(TemporalType.TIMESTAMP)
    @Column(name = "sent")
    private Date sent = new Date();
	
	
	public Message() {};
	
	public Message(String userName, String content) {
		this.userName = userName;
		this.content = content;
	}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getSent() {
        return sent;
    }

    public void setSent(Date sent) {
        this.sent = sent;
    } */

    
}
