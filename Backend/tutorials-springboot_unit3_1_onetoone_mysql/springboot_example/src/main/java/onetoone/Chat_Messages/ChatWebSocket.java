package onetoone.Chat_Messages;


import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import onetoone.Users.UserRepository;
import onetoone.Users.User;
import onetoone.Groups.GroupMembership;
import onetoone.Groups.GroupMembershipRepository;
import onetoone.ImageHandler.ImageMessage;
import onetoone.ImageHandler.ImageMessageRepository;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.websocket.server.PathParam;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.*;

/**
 * @author Chris Lopez
 * Endpoint: ws://server:8080/chat/{userId}
 */

@Component
@ServerEndpoint(value = "/chat/{userId}")
public class ChatWebSocket {

    private static final Logger log =
            LoggerFactory.getLogger(ChatWebSocket.class);


    private static final ObjectMapper payloadParser = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private static MessageRepository messageStore;
    private static UserRepository accountStore;
    private static GroupMembershipRepository groupAccessRepo;

    private static ImageMessageRepository imageStore;

    private static final Map<Integer, Session> liveConnections =
            new ConcurrentHashMap<>();
    private static final Map<Session, Integer> sessionToUserId =
            new ConcurrentHashMap<>();
    private static final Map<Session, String> sessionToUsername =
            new ConcurrentHashMap<>();

    @Autowired
    public void setMessageRepository(MessageRepository repository) {
        ChatWebSocket.messageStore = repository;
    }

    @Autowired
    public void setUserRepository(UserRepository repository) {
        ChatWebSocket.accountStore = repository;
    }

    @Autowired
    public void setGroupMembershipRepository(
            GroupMembershipRepository repository) {
        ChatWebSocket.groupAccessRepo = repository;
    }

    @Autowired
    public void setImageMessageRepository(ImageMessageRepository repository) {

        ChatWebSocket.imageStore = repository;
    }

    /**
     * Handles new WebSocket connections
     *
     */

    @OnOpen
    public void handleOpen(Session clientSession, @PathParam("userId") Integer userId) throws IOException {
        log.info("Connection attempt from userId: {}", userId);

        Optional<User> userLookup = accountStore.findById(userId);
        if (userLookup.isEmpty()) {
            log.warn("User not found: {}", userId);
            clientSession.close(new CloseReason(
                    CloseReason.CloseCodes.CANNOT_ACCEPT,  "User not found"));
            return;
        }

        User account = userLookup.get();
        liveConnections.put(userId, clientSession);
        sessionToUserId.put(clientSession, userId);
        sessionToUsername.put(clientSession, account.getAccountName());
        log.info("User connected: {} (ID: {}, Role: {})",
                account.getAccountName(), account.getUserId(),
                account.getRole());

        sendEvent(clientSession, Map.of(
                "eventType", "CONNECTED",
                "userId", account.getUserId(),
                "username", account.getAccountName(),
                "role", account.getRole()
        ));
    }

    /**
     * Handles incoming WebSocket messages
     */

    @OnMessage
    public void handleMessage(Session clientSession, String incomingText) throws IOException {
        Integer authorId = sessionToUserId.get(clientSession);
        String authorName = sessionToUsername.get(clientSession);

        if (authorId == null || authorName == null) {
            sendError(clientSession, "Unknown session");
            return;
        }

        log.info("Message from {}: {}", authorName, incomingText);

        try {
            JsonNode msgPayload = payloadParser.readTree(incomingText);
            String eventType = text(msgPayload, "eventType", "SEND");

            switch (eventType) {
                case "SEND":
                    processChatMessage(clientSession, authorId,
                            authorName, msgPayload);
                    break;
                case "EDIT":
                    processMessageEdit(clientSession, authorId,
                            authorName, msgPayload);
                    break;
                case "DELETE":
                    processMessageRemoval(clientSession, authorId,
                            msgPayload);
                    break;
                case "UPDATE_PERMISSIONS":
                    processAccessChange(clientSession, authorId,
                            authorName, msgPayload);
                    break;
                case "REMOVE_MEMBER":
                    processGroupKick(clientSession, authorId,
                            authorName, msgPayload);
                    break;
                case "LEAVE":
                    processGroupExit(clientSession, authorId,
                            authorName, msgPayload);
                    break;
                case "JOIN":
                    processGroupEntry(clientSession, authorId,
                            authorName, msgPayload);
                    break;

                case "SEND_IMAGE":
                    processImageMessage(clientSession, authorId,
                            authorName, msgPayload);
                    break;
                default:
                    sendError(clientSession,
                            "Unsupported eventType: " + eventType);
            }
        } catch (Exception e) {
            log.error("WebSocket message processing error: {}",
                    e.getMessage());
            sendError(clientSession, "Invalid message payload");
        }
    }

    /**
     * Handles WebSocket disconnections
     */

    @OnClose
    public void handleClose(Session clientSession) {
        Integer userId = sessionToUserId.remove(clientSession);
        sessionToUsername.remove(clientSession);

        if (userId != null) {
            liveConnections.remove(userId);
            log.info("User disconnected: {}", userId);
        }
    }

    /**
     * Handles WebSocket errors
     */

    @OnError
    public void handleError(Session clientSession, Throwable error) {
        log.error("WebSocket error", error);
    }

    /**
     * Handles sending a message to a group
     */

    private void processChatMessage(Session originatorSession, Integer originatorId, String originatorName, JsonNode msgPayload) {
        try {
            int groupRef = requiredInt(msgPayload, "groupId");
            String msgBody = text(msgPayload, "textBody", "").trim();
            String msgType = Message.normalizeMessageType(
                    text(msgPayload, "messageType", "NORMAL"));
            Integer recipientId = nullableInt(msgPayload, "targetUserId");

            if (msgBody.isEmpty()) {
                sendError(originatorSession, "textBody is required");
                return;
            }

            Optional<GroupMembership> memberLookup = groupAccessRepo.findByGroupIdAndUserId(groupRef, originatorId);
            if (memberLookup.isEmpty()) {
                sendError(originatorSession,
                        "You are not a member of this group");
                return;
            }

            GroupMembership foundMembership = memberLookup.get();

            if (!foundMembership.isChatEnabled()) {
                sendError(originatorSession, "You do not have permission to send messages");
                return;
            }

            if ("ANNOUNCEMENT".equals(msgType) && !foundMembership.isAnnounceEnabled()) {
                sendError(originatorSession, "You do not have permission to post announcements");
                return;
            }

            if ("TRAINER".equals(msgType) && !(foundMembership.isWorkoutEnabled() || GroupMembership.ROLE_TRAINER.equals(
                    foundMembership.getAccessLevel())
                    || foundMembership.isLeaderLike())) {
                sendError(originatorSession, "You do not have permission to send trainer messages");
                return;
            }

            Message storedMsg = messageStore.save(
                    new Message(originatorId, groupRef, recipientId, msgBody, msgType));

            Map<String, Object> outgoingEvent = formatMessageEvent("MESSAGE_CREATED", storedMsg, originatorName);
            distributeToGroup(groupRef, outgoingEvent, null);

        } catch (Exception error) {
            log.error("Error in processChatMessage: {}", error.getMessage());
            sendError(originatorSession, "Error sending message: " + error.getMessage());
        }
    }

    /**
     * Handles sending an image message to a group via WebSocket
     */
    private void processImageMessage(Session originatorSession, Integer originatorId, String originatorName, JsonNode msgPayload) {
        try {
            int groupRef = requiredInt(msgPayload, "groupId");
            String imageUrl = text(msgPayload, "imageUrl", "").trim();
            String caption = text(msgPayload, "caption", null);

            if (imageUrl.isEmpty()) {
                sendError(originatorSession, "imageUrl is required");
                return;
            }

            Optional<GroupMembership> memberLookup = groupAccessRepo.findByGroupIdAndUserId(groupRef, originatorId);
            if (memberLookup.isEmpty()) {
                sendError(originatorSession, "You are not a member of this group");
                return;
            }

            if (!memberLookup.get().isChatEnabled()) {
                sendError(originatorSession, "You do not have permission to send messages");
                return;
            }

            ImageMessage savedImage = imageStore.save(
                    new ImageMessage(
                            originatorId.longValue(),
                            (long) groupRef,
                            imageUrl,
                            caption
                    )
            );

            Map<String, Object> outgoingEvent = new LinkedHashMap<>();
            outgoingEvent.put("eventType", "IMAGE_CREATED");
            outgoingEvent.put("id", savedImage.getMsgKey());
            outgoingEvent.put("senderId", savedImage.getAuthorId());
            outgoingEvent.put("username", originatorName);
            outgoingEvent.put("groupId", savedImage.getChannelId());
            outgoingEvent.put("imageUrl", savedImage.getMediaPath());
            outgoingEvent.put("caption", savedImage.getMsgCaption());
            outgoingEvent.put("sentAt", savedImage.getDispatchTime() != null
                    ? savedImage.getDispatchTime().toString() : null);

            distributeToGroup(groupRef, outgoingEvent, null);

        } catch (Exception error) {
            log.error("Error in processImageMessage: {}", error.getMessage());
            sendError(originatorSession, "Error sending image: " + error.getMessage());
        }
    }


    /**
     * Handles editing an existing message
     */

    private void processMessageEdit(Session originatorSession, Integer originatorId, String originatorName, JsonNode msgPayload) {
        try {
            Long msgId = (long) requiredInt(msgPayload, "messageId");
            String revisedBody = text(msgPayload, "textBody", "").trim();

            Message locatedMsg = messageStore.findById(msgId).orElse(null);
            if (locatedMsg == null ||
                    !Boolean.TRUE.equals(locatedMsg.getActiveFlag())) {
                sendError(originatorSession, "Message not found");
                return;
            }

            if (!Objects.equals(locatedMsg.getOriginatorId(), originatorId)) {
                sendError(originatorSession, "You can only edit your own messages");
                return;
            }

            if (revisedBody.isEmpty()) {
                sendError(originatorSession, "textBody is required");
                return;
            }

            locatedMsg.setTextBody(revisedBody);
            locatedMsg.setEditTimestamp(LocalDateTime.now());
            locatedMsg = messageStore.save(locatedMsg);

            Map<String, Object> outgoingEvent = formatMessageEvent(
                    "MESSAGE_UPDATED", locatedMsg, originatorName);
            distributeToGroup(locatedMsg.getGroupId(), outgoingEvent, null);

        } catch (Exception error) {
            log.error("Error in processMessageEdit: {}", error.getMessage());
            sendError(originatorSession, "Error editing message: " + error.getMessage());
        }
    }

    /**
     * Handles deleting a message
     */

    private void processMessageRemoval(Session originatorSession, Integer originatorId, JsonNode msgPayload) {
        try {
            Long msgId = (long) requiredInt(msgPayload, "messageId");

            Message locatedMsg = messageStore.findById(msgId).orElse(null);
            if (locatedMsg == null ||
                    !Boolean.TRUE.equals(locatedMsg.getActiveFlag())) {
                sendError(originatorSession, "Message not found");
                return;
            }

            if (!Objects.equals(locatedMsg.getOriginatorId(), originatorId)) {
                sendError(originatorSession, "You can only delete your own messages");
                return;
            }

            locatedMsg.setActiveFlag(false);
            messageStore.save(locatedMsg);

            Map<String, Object> outgoingEvent = new LinkedHashMap<>();
            outgoingEvent.put("eventType", "MESSAGE_DELETED");
            outgoingEvent.put("messageId", locatedMsg.getId());
            outgoingEvent.put("groupId", locatedMsg.getGroupId());
            distributeToGroup(locatedMsg.getGroupId(), outgoingEvent, null);

        } catch (Exception error) {
            log.error("Error in processMessageRemoval: {}", error.getMessage());
            sendError(originatorSession, "Error deleting message: " + error.getMessage());
        }
    }

    /**
     * Handles updating member permissions
     */

    private void processAccessChange(Session originatorSession, Integer originatorId, String originatorName, JsonNode msgPayload) {
        try {
            int groupRef = requiredInt(msgPayload, "groupId");
            int targetUserId = requiredInt(msgPayload, "targetUserId");

            Optional<GroupMembership> supervisorLookup = groupAccessRepo.findByGroupIdAndUserId(groupRef, originatorId);
            if (supervisorLookup.isEmpty() || !supervisorLookup.get().isManageEnabled()) {
                sendError(originatorSession, "You do not have permission to update permissions");
                return;
            }

            Optional<GroupMembership> targetLookup = groupAccessRepo.findByGroupIdAndUserId(groupRef, targetUserId);
            if (targetLookup.isEmpty()) {
                sendError(originatorSession, "Target user is not a member of this group");
                return;
            }

            GroupMembership targetMembership = targetLookup.get();
            if (targetMembership.isLeaderLike()) {
                sendError(originatorSession, "Cannot change GROUP_LEADER or ADMIN permissions");
                return;
            }

            targetMembership.setAccessLevel(GroupMembership.normalizeRole(
                    text(msgPayload, "membershipRole", targetMembership.getAccessLevel())));
            targetMembership.setChatEnabled(bool(msgPayload, "canSendMessages", targetMembership.isChatEnabled()));
            targetMembership.setManageEnabled(bool(msgPayload, "canManageMembers", targetMembership.isManageEnabled()));
            targetMembership.setAnnounceEnabled(bool(msgPayload, "canPostAnnouncements",
                    targetMembership.isAnnounceEnabled()));
            targetMembership.setWorkoutEnabled(bool(msgPayload, "canAssignWorkouts", targetMembership.isWorkoutEnabled()));
            groupAccessRepo.save(targetMembership);

            Optional<User> targetAccount =
                    accountStore.findById(targetUserId);

            Map<String, Object> outgoingEvent = new LinkedHashMap<>();
            outgoingEvent.put("eventType", "GROUP_PERMISSION_UPDATED");
            outgoingEvent.put("groupId", groupRef);
            outgoingEvent.put("targetUserId", targetUserId);
            outgoingEvent.put("username", targetAccount.map(User::getAccountName).orElse("Unknown"));
            outgoingEvent.put("membershipRole", targetMembership.getAccessLevel());
            outgoingEvent.put("canSendMessages", targetMembership.isChatEnabled());
            outgoingEvent.put("canManageMembers", targetMembership.isManageEnabled());
            outgoingEvent.put("canPostAnnouncements", targetMembership.isAnnounceEnabled());
            outgoingEvent.put("canAssignWorkouts", targetMembership.isWorkoutEnabled());
            outgoingEvent.put("changedByUserId", originatorId);
            outgoingEvent.put("changedByUsername", originatorName);

            distributeToGroup(groupRef, outgoingEvent, null);

        } catch (Exception error) {
            log.error("Error in processAccessChange: {}", error.getMessage());
            sendError(originatorSession, "Error updating permissions: " + error.getMessage());
        }
    }

    /**
     * Handles removing a member from a group
     */

    private void processGroupKick(Session originatorSession, Integer originatorId, String originatorName, JsonNode msgPayload) {
        try {
            int groupRef = requiredInt(msgPayload, "groupId");
            int kickedUserId = requiredInt(msgPayload, "targetUserId");

            Optional<GroupMembership> supervisorLookup = groupAccessRepo.findByGroupIdAndUserId(groupRef, originatorId);
            if (supervisorLookup.isEmpty() || !supervisorLookup.get().isManageEnabled()) {
                sendError(originatorSession, "You do not have permission to remove members");
                return;
            }

            Optional<GroupMembership> kickedMemberLookup = groupAccessRepo.findByGroupIdAndUserId(groupRef, kickedUserId);
            if (kickedMemberLookup.isEmpty()) {
                sendError(originatorSession, "Target user is not a member of this group");
                return;
            }

            if (kickedMemberLookup.get().isLeaderLike()) {
                sendError(originatorSession, "Cannot remove GROUP_LEADER or ADMIN");
                return;
            }

            groupAccessRepo.delete(kickedMemberLookup.get());
            Optional<User> kickedAccount = accountStore.findById(kickedUserId);

            Map<String, Object> outgoingEvent = new LinkedHashMap<>();
            outgoingEvent.put("eventType", "GROUP_MEMBER_REMOVED");
            outgoingEvent.put("groupId", groupRef);
            outgoingEvent.put("targetUserId", kickedUserId);
            outgoingEvent.put("username", kickedAccount.map(User::getAccountName).orElse("Unknown"));
            outgoingEvent.put("removedByUserId", originatorId);
            outgoingEvent.put("removedByUsername", originatorName);

            distributeToGroup(groupRef, outgoingEvent, null);

        } catch (Exception error) {
            log.error("Error in processGroupKick: {}", error.getMessage());
            sendError(originatorSession, "Error removing member: " + error.getMessage());
        }
    }

    /**
     * Handles a user leaving a group
     */

    private void processGroupExit(Session originatorSession, Integer originatorId, String originatorName, JsonNode msgPayload) {
        try {
            int groupRef = requiredInt(msgPayload, "groupId");

            Optional<GroupMembership> exitMemberLookup = groupAccessRepo.findByGroupIdAndUserId(groupRef, originatorId);
            if (exitMemberLookup.isEmpty()) {
                sendError(originatorSession, "You are not a member of this group");
                return;
            }

            groupAccessRepo.delete(exitMemberLookup.get());

            Map<String, Object> outgoingEvent = new LinkedHashMap<>();
            outgoingEvent.put("eventType", "GROUP_MEMBER_LEFT");
            outgoingEvent.put("groupId", groupRef);
            outgoingEvent.put("userId", originatorId);
            outgoingEvent.put("username", originatorName);

            distributeToGroup(groupRef, outgoingEvent, null);

        } catch (Exception error) {
            log.error("Error in processGroupExit: {}", error.getMessage());
            sendError(originatorSession, "Error leaving group: " + error.getMessage());
        }
    }

    /**
     * Handles a user joining a group
     */

    private void processGroupEntry(Session originatorSession, Integer originatorId, String originatorName, JsonNode msgPayload) {
        try {
            int groupRef = requiredInt(msgPayload, "groupId");

            if (groupAccessRepo.existsByGroupIdAndUserId(
                    groupRef, originatorId)) {
                sendError(originatorSession, "You are already a member of this group");
                return;
            }

            Optional<User> joinerLookup = accountStore.findById(originatorId);
            String joinerRole = joinerLookup.map(User::getRole).orElse("Client");

            String entryLevel = "Trainer".equalsIgnoreCase(joinerRole) ? GroupMembership.ROLE_TRAINER : GroupMembership.ROLE_MEMBER;

            GroupMembership newMembership = groupAccessRepo.save(
                    new GroupMembership(groupRef, originatorId, entryLevel));

            Map<String, Object> outgoingEvent = new LinkedHashMap<>();
            outgoingEvent.put("eventType", "GROUP_MEMBER_JOINED");
            outgoingEvent.put("groupId", groupRef);
            outgoingEvent.put("membershipId", newMembership.getMembershipId());
            outgoingEvent.put("userId", originatorId);
            outgoingEvent.put("username", originatorName);
            outgoingEvent.put("membershipRole", newMembership.getAccessLevel());

            distributeToGroup(groupRef, outgoingEvent, null);

        } catch (Exception error) {
            log.error("Error in processGroupEntry: {}", error.getMessage());
            sendError(originatorSession, "Error joining group: " + error.getMessage());
        }
    }

    /**
     * Broadcasts to all active members of a group
     */

    private void distributeToGroup(int groupRef,
                                   Map<String, Object> outgoingEvent, Session excludedSession) {
        List<GroupMembership> groupMemberList = groupAccessRepo.findByGroupId(groupRef);

        for (GroupMembership membership : groupMemberList) {
            Session memberSession = liveConnections.get(membership.getUserId());

            if (memberSession != null && memberSession.isOpen() && !memberSession.equals(excludedSession)) {
                sendEvent(memberSession, outgoingEvent);
            }
        }
    }

    /**
     * Builds a JSON message event map from a Message object
     */

    private Map<String, Object> formatMessageEvent(String eventType,
                                                   Message msg, String authorName) {
        Map<String, Object> msgData = new LinkedHashMap<>();
        msgData.put("id", msg.getId());
        msgData.put("originatorId", msg.getOriginatorId());
        msgData.put("username", authorName);
        msgData.put("groupId", msg.getGroupId());
        msgData.put("targetUserId", msg.getTargetUserId());
        msgData.put("messageType", msg.getMessageType());
        msgData.put("textBody", msg.getTextBody());

        msgData.put("createdTimestamp",
                msg.getCreatedTimestamp() != null ? msg.getCreatedTimestamp().toString() : null);

        msgData.put("editTimestamp",
                msg.getEditTimestamp() != null ? msg.getEditTimestamp().toString() : null);

        Map<String, Object> outgoingEvent = new LinkedHashMap<>();
        outgoingEvent.put("eventType", eventType);
        outgoingEvent.put("message", msgData);
        return outgoingEvent;
    }


    private void sendEvent(Session destination,
                           Map<String, Object> outgoingEvent) {
        if (destination == null || !destination.isOpen()) return;
        try {
            destination.getBasicRemote().sendText(payloadParser.writeValueAsString(outgoingEvent));
        } catch (IOException e) {
            log.error("Error sending WebSocket event", e);
        }
    }

    private void sendError(Session session, String errorMsg) {
        sendEvent(session, Map.of("eventType", "ERROR", "message", errorMsg
        ));
    }


    private int requiredInt(JsonNode node, String fieldName) {
        if (!node.has(fieldName) || node.get(fieldName).isNull()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return node.get(fieldName).asInt();
    }

    private Integer nullableInt(JsonNode node, String fieldName) {
        if (!node.has(fieldName))
         return null;
        JsonNode field = node.get(fieldName);
        if (field == null || field.isNull())
         return null;
        return field.asInt();
    }

    private String text(JsonNode node, String fieldName,
                        String defaultValue) {
        return node.has(fieldName) && !node.get(fieldName).isNull() ? node.get(fieldName).asText() : defaultValue;
    }

    /**
     * Gets a boolean from JSON payload with default value
     */

    private boolean bool(JsonNode node, String fieldName, boolean defaultValue) {
        return node.has(fieldName) && !node.get(fieldName).isNull() ? node.get(fieldName).asBoolean() : defaultValue;
    }
}
