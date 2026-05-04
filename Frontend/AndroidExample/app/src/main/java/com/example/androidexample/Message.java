package com.example.androidexample;

import org.json.JSONException;
import org.json.JSONObject;

public class Message {
    private long messageId;
    private int originatorId;
    private Integer groupId;
    private Integer targetUserId;

    private String username;

    private String messageType;

    private String textBody;

    private String createdTimestamp;

    private String editTimestamp;

    // Image message fields (used when messageType == "IMAGE")
    private long imageMessageId;  // backend ID from the images table
    private String imageUrl;
    private String caption;

    public Message(){}
    public Message(
            long messageId, int originatorId, Integer groupId, Integer targetUserId, String username,
            String messageType, String textBody, String createdTimestamp, String editTimestamp) {
        this.messageId = messageId;
        this.originatorId = originatorId;
        this.groupId = groupId;
        this.targetUserId = targetUserId;
        this.messageType = messageType;
        this.textBody = textBody;
        this.createdTimestamp = createdTimestamp;
        this.editTimestamp = editTimestamp;
        this.username = username;
    }

    public static Message fromJson(JSONObject json) throws JSONException {
        Message message = new Message();

        // handle id vs messageId  better safe than sorry
        if (json.has("messageId")) {
            message.messageId = json.optLong("messageId", -1);
        } else {
            message.messageId = json.optLong("id", -1);
        }

        message.originatorId = json.optInt("originatorId", -1);

        if (!json.isNull("groupId")) {
            message.groupId = json.optInt("groupId");
        }

        if (!json.isNull("targetUserId")) {
            message.targetUserId = json.optInt("targetUserId");
        } else {
            message.targetUserId = null; // IMPORTANT
        }

        message.messageType = json.optString("messageType", "NORMAL");
        message.textBody = json.optString("textBody", "");

        // username
        message.username = json.optString("username", "");

        message.createdTimestamp = json.optString("createdTimestamp", "");

        if (!json.isNull("editTimestamp")) {
            message.editTimestamp = json.optString("editTimestamp");
        } else {
            message.editTimestamp = null;
        }

        return message;
    }

    public JSONObject toJson() {
        JSONObject obj = new JSONObject();

        try {
            obj.put("messageId", messageId);
            obj.put("originatorId", originatorId);
            obj.put("username", username);
            //groupId shouldn't be null unless we make single-single groups probably
            if (groupId != null) {
                obj.put("groupId", groupId);
            } else {
                obj.put("groupId", JSONObject.NULL);
            }

            if (targetUserId != null) {
                obj.put("targetUserId", targetUserId);
            } else {
                obj.put("targetUserId", JSONObject.NULL);
            }

            obj.put("messageType", messageType);
            obj.put("textBody", textBody);
            obj.put("createdTimestamp", createdTimestamp);

            if (editTimestamp != null) {
                obj.put("editTimestamp", editTimestamp);
            } else {
                obj.put("editTimestamp", JSONObject.NULL);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return obj;
    }

    public long getMessageId() {
        return messageId;
    }

    public int getOriginatorId() {
        return originatorId;
    }
    public Integer getGroupId() {
        return groupId;
    }
    public Integer getTargetUserId() {
        return targetUserId;
    }
    //be specific on message
    public String getUsername() {
        return username;
    }
    public String getMessageType() {
        return messageType;
    }
    public String getTextBody() {
        return textBody;
    }
    public String getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }
    public void setOriginatorId(int originatorId) {
        this.originatorId = originatorId;
    }
    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }
    public void setTargetUserId(Integer targetUserId) {
        this.targetUserId = targetUserId;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
    public void setTextBody(String textBody) {
        this.textBody = textBody;
    }
    public void setCreatedTimestamp(String createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }
    public void setEditTimestamp(String editTimestamp) {
        this.editTimestamp = editTimestamp;
    }

    public long getImageMessageId() { return imageMessageId; }
    public void setImageMessageId(long imageMessageId) { this.imageMessageId = imageMessageId; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }

    @Override
    public String toString() {
        return "MessageItem{" +
                "id=" + messageId +
                ", originatorId=" + originatorId +
                ", username='" + username + '\'' +
                ", groupId=" + groupId +
                ", targetUserId=" + targetUserId +
                ", messageType='" + messageType + '\'' +
                ", textBody='" + textBody + '\'' +
                ", createdTimestamp='" + createdTimestamp + '\'' +
                ", editTimestamp='" + editTimestamp + '\'' +
                '}';
    }














}
