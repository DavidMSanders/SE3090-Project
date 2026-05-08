package onetoone.Chat_Messages;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import jakarta.persistence.*;

//import lombok.Data;

/**
 *  @author Chris Lopez
 */

@Entity
@Table(name = "messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sender_id", nullable = false)
    private Integer originatorId;

    @Column(name = "author_id", nullable = false)
    private Integer authorId;

    @Column(name = "group_id")
    private Integer groupRef;


    @Column(name = "recipient_id", columnDefinition = "INT DEFAULT NULL")
    private Integer targetUserId;

    @Lob
    @Column(name = "message_text", nullable = false)
    private String textBody;

    @Column(name = "time_stamp", nullable = false)
    private LocalDateTime createdTimestamp = LocalDateTime.now();

    @Column(name = "is_deleted")
    private Boolean activeFlag = true;

    @Column(name = "is_read")
    private Boolean readFlag = false;

    @Column(name = "message_type")
    private String msgCategory = "NORMAL";

    @Column(name = "edited_at")
    private LocalDateTime editTimestamp;


    public Message() {
    }

    public Message(Integer originatorId, Integer targetUserId, String textBody) {
        this.originatorId = originatorId;
        this.authorId = originatorId;
        this.targetUserId = targetUserId;
        this.textBody = textBody;
    }


    public Message(Integer originatorId, Integer groupId, Integer targetUserId, String textBody, String messageType) {
        this.originatorId = originatorId;
        this.authorId = originatorId;
        this.groupRef = groupId;
        this.targetUserId = targetUserId;
        this.textBody = textBody;
        this.msgCategory = messageType;
    }

    @PrePersist
    @PreUpdate
    private void syncAuthorId() {
        if (originatorId != null) {
            this.authorId = originatorId;
        } else if (authorId != null) {
            this.originatorId = authorId;
        }
    }

    public static String normalizeMessageType(String type) {
        if (type == null) return "NORMAL";
        switch (type.toUpperCase()) {
            case "ANNOUNCEMENT":
                return "ANNOUNCEMENT";
            case "DIRECT":
                return "DIRECT";
            case "TRAINER":
                return "TRAINER";
            default:
                return "NORMAL";
        }
    }

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public Integer getOriginatorId() {

        return originatorId;
    }

    public void setOriginatorId(Integer originatorId) {
        this.originatorId = originatorId;
        if (originatorId != null) this.authorId = originatorId;
    }

    public Integer getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Integer authorId) {
        this.authorId = authorId;
        if (authorId != null) this.originatorId = authorId;
    }

    public Integer getGroupId() {

        return groupRef;
    }

    public void setGroupId(Integer groupRef) {

        this.groupRef = groupRef;
    }

    public Integer getTargetUserId() {

        return targetUserId;
    }

    public void setTargetUserId(Integer targetUserId) {

        this.targetUserId = targetUserId;
    }

    public String getTextBody() {
        return textBody;
    }

    public void setTextBody(String textBody) {

        this.textBody = textBody;
    }

    public LocalDateTime getCreatedTimestamp() {

        return createdTimestamp;
    }

    public void setCreatedTimestamp(LocalDateTime createdTimestamp) {

        this.createdTimestamp = createdTimestamp;
    }

    public Boolean getActiveFlag() {

        return activeFlag;
    }

    public void setActiveFlag(Boolean activeFlag) {

        this.activeFlag = activeFlag;
    }

    public Boolean getReadFlag() {

        return readFlag;
    }

    public void setReadFlag(Boolean readFlag) {

        this.readFlag = readFlag;
    }

    public String getMessageType() {

        return msgCategory;
    }

    public void setMessageType(String msgCategory) {

        this.msgCategory = msgCategory;
    }

    public LocalDateTime getEditTimestamp() {

        return editTimestamp;
    }

    public void setEditTimestamp(LocalDateTime editTimestamp) {

        this.editTimestamp = editTimestamp;
    }

    @Override
    public String toString() {
        return "Message{" + "id=" + id + ", originatorId=" + originatorId + ", targetUserId=" + targetUserId +
                ", createdTimeStamp" + createdTimestamp + ", readFlag=" + readFlag +
                '}';
    }
}

