package onetoone.ImageHandler;
import onetoone.Users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles all image message endpoints through chat
 * Extends existing chat system with image support
 * @author Chris Lopez
 */

@RestController
@RequestMapping("/api/v1")
public class ImageMessageController {
    @Autowired
    private ImageMessageRepository mediaRepo;

    @Autowired
    private UserRepository userRepo;


    /**
     * Sends an image message to a group chat
     */
    @PostMapping("/images/send")
    public ResponseEntity<Map<String, Object>> sendImage(@RequestBody Map<String, Object> body) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long authorId = Long.valueOf(body.get("senderId").toString());

            if (!userRepo.existsById(authorId.intValue())) {
                result.put("message", "Sender not found");
                return ResponseEntity.status(
                        HttpStatus.NOT_FOUND).body(result);
            }

            Long channelId = body.containsKey("groupId") && body.get("groupId") != null ?
                    Long.valueOf(body.get("groupId").toString()) :
                    null;

            String mediaPath = body.get("imageUrl").toString();

            String msgCaption = body.containsKey("caption") ? body.get("caption").toString() : null;

            ImageMessage newEntry = new ImageMessage(authorId, channelId, mediaPath, msgCaption);
            newEntry = mediaRepo.save(newEntry);

            result.put("message", "Image sent successfully");
            result.putAll(buildMediaMap(newEntry));
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            result.put("message", "Error sending image: " + e.getMessage());
            return ResponseEntity.status(
                    HttpStatus.BAD_REQUEST).body(result);
        }
    }

    /**
     * 2 - Get all image messages in a group chat
     */
    @GetMapping("/groups/{groupId}/images")
    public ResponseEntity<List<Map<String, Object>>> getGroupImages(@PathVariable Long groupId) {
        List<ImageMessage> mediaList = mediaRepo.findByChannelIdOrderByDispatchTimeDesc(groupId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (ImageMessage entry : mediaList) {
            result.add(buildMediaMap(entry));
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 3 - Get a single image message by ID
     */
    @GetMapping("/images/{id}")
    public ResponseEntity<Map<String, Object>> getImageById(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        ImageMessage locatedEntry = mediaRepo.findById(id).orElse(null);
        if (locatedEntry == null) {
            result.put("message", "Image not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
        result.putAll(buildMediaMap(locatedEntry));
        return ResponseEntity.ok(result);
    }

    /**
     * 4 - Get all images sent by a specific user
     */
    @GetMapping("/users/{userId}/images")
    public ResponseEntity<List<Map<String, Object>>> getUserImages(@PathVariable Long userId) {
        List<ImageMessage> mediaList = mediaRepo.findByAuthorId(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (ImageMessage entry : mediaList) {
            result.add(buildMediaMap(entry));
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 5 - Update the caption of an image message
     */
    @PutMapping("/images/{id}/caption")
    public ResponseEntity<Map<String, Object>> updateCaption(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Map<String, Object> result = new HashMap<>();
        ImageMessage foundEntry = mediaRepo.findById(id).orElse(null);
        if (foundEntry == null) {
            result.put("message", "Image not found");
            return ResponseEntity.status(
                    HttpStatus.NOT_FOUND).body(result);
        }
        if (body.containsKey("caption")) {
            foundEntry.setMsgCaption(body.get("caption").toString());
        }
        foundEntry = mediaRepo.save(foundEntry);
        result.put("message", "Caption updated successfully");
        result.putAll(buildMediaMap(foundEntry));
        return ResponseEntity.ok(result);
    }

    /**
     * 6 - Delete an image message
     */
    @DeleteMapping("/images/{id}")
    public ResponseEntity<Map<String, Object>> deleteImage(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        ImageMessage foundEntry = mediaRepo.findById(id).orElse(null);
        if (foundEntry == null) {
            result.put("message", "Image not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
        mediaRepo.delete(foundEntry);
        result.put("message", "Image message deleted successfully");
        result.put("id", id);
        return ResponseEntity.ok(result);
    }

    private Map<String, Object> buildMediaMap(ImageMessage entry) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", entry.getMsgKey());
        map.put("senderId", entry.getAuthorId());
        map.put("groupId", entry.getChannelId());
        map.put("imageUrl", entry.getMediaPath());
        map.put("caption", entry.getMsgCaption());
        map.put("sentAt", entry.getDispatchTime() != null ? entry.getDispatchTime().toString() : null);
        return map;
    }
}
