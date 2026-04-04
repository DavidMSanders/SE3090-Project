package onetoone.Chat_Messages;

import onetoone.Users.User;
import onetoone.Users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.responses.ApiResponse;
//import io.swagger.v3.oas.annotations.responses.ApiResponses;


@RestController
@RequestMapping("/api/v1/messages")
public class MessageController {

    @Autowired
    private MessageRepository msgRepo;

    @Autowired
    private UserRepository userRepo;


    /**
     * Get all messages for agroup ordered oldest to newest
     * Includes username of sender
     */
    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<Map<String, Object>>> loadGroupHistory(@PathVariable Integer groupId){
        List<Map<String, Object>> result  = new ArrayList<>();


        List<Message> msgHistory = msgRepo.findByGroupRefOrderByCreatedTimestampAsc(groupId);

        for(Message entry : msgHistory){
            Map<String, Object> msgData = new HashMap<>();
            msgData.put("id", entry.getId());
            msgData.put("originatorId", entry.getOriginatorId());
            msgData.put("groupId", entry.getGroupId());
            msgData.put("targetUserId", entry.getTargetUserId());
            msgData.put("messageType", entry.getMessageType());
            msgData.put("textBody", entry.getTextBody());
            msgData.put("createdTimestamp", entry.getCreatedTimestamp());
            msgData.put("editTimestamp", entry.getEditTimestamp());

            Optional<User> senderQuery = userRepo.findById(entry.getOriginatorId());
            msgData.put("username", senderQuery.map(User::getAccountName).orElse("Unknown"));
            result.add(msgData);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * Get  a single message by ID
     */
    @GetMapping("/{messageId}")
    public ResponseEntity<Map<String, Object>> loadSingleMsg(@PathVariable Long messageId){
        Map<String, Object> result = new HashMap<>();


        Message found = msgRepo.findById(messageId).orElse(null);
        if(found != null){
            Optional<User> senderQuery = userRepo.findById(found.getOriginatorId());
            result.put("id", found.getId());
            result.put("originatorId", found.getOriginatorId());
            result.put("username",
                    senderQuery.map(User::getAccountName).orElse("Unknown"));
            result.put("groupId", found.getGroupId());
            result.put("targetUserId", found.getTargetUserId());
            result.put("messageType", found.getMessageType());
            result.put("textBody", found.getTextBody());
            result.put("createdTimestamp", found.getCreatedTimestamp());
            result.put("editTimestamp", found.getEditTimestamp());
            return ResponseEntity.ok(result);
        } else {
            result.put("message", "Message not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
    }

    /**
     * Delete a message by ID
     * Only the original sender can delete their message
     */
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Map<String, Object>> dropMessage(
            @PathVariable Long messageId, @RequestBody SenderBody incoming){
        Map<String, Object> result = new HashMap<>();
        Message found = msgRepo.findById(messageId).orElse(null);
        if (found == null) {
            result.put("message", "Message not found");

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }

        if (found.getOriginatorId() != incoming.getSenderId()) {
            result.put("message", "You can only delete your own messages");

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
        }

        result.put("eventType", "MESSAGE_DELETED");
        result.put("messageId", messageId);
        result.put("groupId", found.getGroupId());
        msgRepo.delete(found);

            return ResponseEntity.ok(result);
    }



    @PutMapping("/{messageId}")
    public ResponseEntity<Map<String, Object>> amendMessage(@PathVariable Long messageId, @RequestBody AmendBody incoming){
    Map<String, Object> result = new HashMap<>();
        Message found = msgRepo.findById(messageId).orElse(null);
        if (found == null) {
            result.put("message", "Message not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }

        if (found.getOriginatorId() != incoming.getSenderId()) {
            result.put("message", "You can only edit your own messages");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
        }

        found.setTextBody(incoming.getUpdatedBody());
        found.setEditTimestamp(java.time.LocalDateTime.now());
        msgRepo.save(found);

        Optional<User> senderQuery = userRepo.findById(found.getOriginatorId());
        result.put("eventType", "MESSAGE_UPDATED");
        result.put("id", found.getId());
        result.put("originatorId", found.getOriginatorId());
        result.put("username",
                senderQuery.map(User::getAccountName).orElse("Unknown"));
        result.put("groupId", found.getGroupId());
        result.put("targetUserId", found.getTargetUserId());
        result.put("messageType", found.getMessageType());
        result.put("textBody", found.getTextBody());
        result.put("createdTimestamp", found.getCreatedTimestamp());
        result.put("editTimestamp", found.getEditTimestamp());
        return ResponseEntity.ok(result);
    }


/**
 * Request body for deleting message
 */
class SenderBody{
    private int senderId;
    private int getSenderId(){
        return senderId;
    }

    public void setSenderId(int senderId){
    this.senderId = senderId;


    }
}

/**
 * Request body for editing a message
 */

class AmendBody{
    private int senderId;
    private String updatedBody;


    public int getSenderId(){
        return senderId;
    }

    public void setSenderId(int senderId){
        this.senderId = senderId;
    }

    public String getUpdatedBody() {
        return updatedBody;
    }

    public void setUpdatedBody(String updatedBody){
        this.updatedBody = updatedBody;
    }
    }
}
