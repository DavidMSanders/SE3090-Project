
package com.example.androidexample;


import org.json.JSONException;
import org.json.JSONObject;


public class ChatSocketEvent {

    private String eventType;
    private Message message;
    private Long messageId;
    private Integer groupId;

    private Integer targetUserId;
    private String membershipRole;
    private Boolean canSendMessages;
    private Boolean canManageMembers;
    private Boolean canPostAnnouncements;
    private Boolean canAssignWorkouts;

    // ADDED: CONNECTED event fields
    private Integer userId;
    private String username;
    private String role;

    // ADDED: ERROR event field
    private String errorMessage;

    public ChatSocketEvent() {}

    public String getEventType() { return eventType; }
    public Message getMessage() { return message; }
    public Long getMessageId() { return messageId; }
    public Integer getGroupId() { return groupId; }
    public Integer getTargetUserId() { return targetUserId; }
    public String getMembershipRole() { return membershipRole; }

    // ADDED: getters for CONNECTED event
    public Integer getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getRole() { return role; }

    // ADDED: getter for ERROR event
    public String getErrorMessage() { return errorMessage; }

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

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
    public void setMessage(Message message) { this.message = message; }
    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }
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

    public static ChatSocketEvent fromJson(JSONObject json)
            throws JSONException {
        ChatSocketEvent event = new ChatSocketEvent();

        event.setEventType(json.optString("eventType", ""));

        //parse CONNECTED event fields
        if (json.has("userId") && !json.isNull("userId")) {
            event.userId = json.getInt("userId");
        }
        if (json.has("username") && !json.isNull("username")) {
            event.username = json.optString("username");
        }
        if (json.has("role") && !json.isNull("role")) {
            event.role = json.optString("role");
        }

        //was always trying to parse "message" as JSONObject
        //Backend sends "message" as String for ERROR events
        // and as JSONObject for MESSAGE_CREATED/UPDATED events
        if (json.has("message") && !json.isNull("message")) {
            Object messageField = json.get("message");
            if (messageField instanceof JSONObject) {
                //MESSAGE_CREATED/UPDATED — parse as Message object
                event.setMessage(Message.fromJson(
                        (JSONObject) messageField));
            } else if (messageField instanceof String) {
                //ERROR event — store as error string
                event.errorMessage = (String) messageField;
            }
        }

        if (json.has("messageId") && !json.isNull("messageId")) {
            event.setMessageId(json.getLong("messageId"));
        }

        if (json.has("groupId") && !json.isNull("groupId")) {
            event.setGroupId(json.getInt("groupId"));
        }

        if (json.has("targetUserId") && !json.isNull("targetUserId")) {
            event.setTargetUserId(json.getInt("targetUserId"));
        }

        if (json.has("membershipRole") && !json.isNull("membershipRole")) {
            event.setMembershipRole(json.getString("membershipRole"));
        }

        if (json.has("canSendMessages") &&
                !json.isNull("canSendMessages")) {
            event.setCanSendMessages(json.getBoolean("canSendMessages"));
        }
        if (json.has("canManageMembers") &&
                !json.isNull("canManageMembers")) {
            event.setCanManageMembers(
                    json.getBoolean("canManageMembers"));
        }
        if (json.has("canPostAnnouncements") &&
                !json.isNull("canPostAnnouncements")) {
            event.setCanPostAnnouncements(
                    json.getBoolean("canPostAnnouncements"));
        }
        if (json.has("canAssignWorkouts") &&
                !json.isNull("canAssignWorkouts")) {
            event.setCanAssignWorkouts(
                    json.getBoolean("canAssignWorkouts"));
        }

        return event;
    }
}