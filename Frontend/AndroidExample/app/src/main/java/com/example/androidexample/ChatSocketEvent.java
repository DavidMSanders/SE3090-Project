package com.example.androidexample;

import org.json.JSONException;
import org.json.JSONObject;

public class ChatSocketEvent {

    //chat event fields
    private String eventType; // MESSAGE_CREATED, MESSAGE_UPDATED, MESSAGE_DELETED, etc.
    private Message message;
    private Long messageId;
    private Integer groupId;

    //permission-update fields
    private Integer targetUserId;
    private String membershipRole;
    private Boolean canSendMessages;
    private Boolean canManageMembers;
    private Boolean canPostAnnouncements;
    private Boolean canAssignWorkouts;



    public ChatSocketEvent() {}

    public String getEventType() { return eventType; }
    public Message getMessage() { return message; }
    public Long getMessageId() { return messageId; }
    public Integer getGroupId() { return groupId; }

    public Integer getTargetUserId() {
        return targetUserId;
    }

    public String getMembershipRole() {
        return membershipRole;
    }

    public boolean isCanSendMessages() {
        return canSendMessages != null && canSendMessages;
    }

    public boolean isCanManageMembers() {
        return canManageMembers != null && canManageMembers;
    }

    public boolean isCanPostAnnouncements() {
        return canPostAnnouncements != null && canPostAnnouncements;
    }

    public boolean isCanAssignWorkouts() {
        return canAssignWorkouts != null && canAssignWorkouts;
    }


    public void setEventType(String eventType) { this.eventType = eventType; }
    public void setMessage(Message message) { this.message = message; }
    public void setMessageId(Long messageId) { this.messageId = messageId; }
    public void setGroupId(Integer groupId) { this.groupId = groupId; }

    public void setTargetUserId(Integer targetUserId) {
        this.targetUserId = targetUserId;
    }
    public void setMembershipRole(String membershipRole) {
        this.membershipRole = membershipRole;
    }
    public void setCanSendMessages(Boolean canSendMessages) {
        this.canSendMessages = canSendMessages;
    }
    public void setCanManageMembers(Boolean canManageMembers) {
        this.canManageMembers = canManageMembers;
    }
    public void setCanPostAnnouncements(Boolean canPostAnnouncements) {
        this.canPostAnnouncements = canPostAnnouncements;
    }
    public void setCanAssignWorkouts(Boolean canAssignWorkouts) {
        this.canAssignWorkouts = canAssignWorkouts;
    }


    public static ChatSocketEvent fromJson(JSONObject json) throws JSONException {
        ChatSocketEvent event = new ChatSocketEvent();

        event.setEventType(json.optString("eventType", ""));

        if (json.has("message") && !json.isNull("message")) {
            event.setMessage(Message.fromJson(json.getJSONObject("message")));
        }

        if (json.has("messageId") && !json.isNull("messageId")) {
            event.setMessageId(json.getLong("messageId"));
        }

        if (json.has("groupId") && !json.isNull("groupId")) {
            event.setGroupId(json.getInt("groupId"));
        }

        // Permission update fields
        if (json.has("targetUserId") && !json.isNull("targetUserId")) {
            event.setTargetUserId(json.getInt("targetUserId"));
        }

        if (json.has("membershipRole") && !json.isNull("membershipRole")) {
            event.setMembershipRole(json.getString("membershipRole"));
        }

        if (json.has("canSendMessages") && !json.isNull("canSendMessages")) {
            event.setCanSendMessages(json.getBoolean("canSendMessages"));
        }

        if (json.has("canManageMembers") && !json.isNull("canManageMembers")) {
            event.setCanManageMembers(json.getBoolean("canManageMembers"));
        }

        if (json.has("canPostAnnouncements") && !json.isNull("canPostAnnouncements")) {
            event.setCanPostAnnouncements(json.getBoolean("canPostAnnouncements"));
        }

        if (json.has("canAssignWorkouts") && !json.isNull("canAssignWorkouts")) {
            event.setCanAssignWorkouts(json.getBoolean("canAssignWorkouts"));
        }

        return event;
    }
}

