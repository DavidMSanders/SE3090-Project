package onetoone.Notifications;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @GetMapping("/{userId}")
    public ResponseEntity<List<Notification>> getNotificationHistory(@PathVariable int userId) {
        return ResponseEntity.ok(notificationService.getHistory(userId));
    }

    @GetMapping("/{userId}/filter")
    public ResponseEntity<List<Notification>> filterNotifications(
            @PathVariable int userId,
            @RequestParam boolean isRead) {
        return ResponseEntity.ok(notificationRepository.findByUserIdAndIsReadOrderByTimestampDesc(userId, isRead));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<String> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok("Notification marked as read");
    }

    @PutMapping("/{id}/unread")
    public ResponseEntity<String> markAsUnread(@PathVariable Long id) {
        notificationService.markAsUnread(id);
        return ResponseEntity.ok("Notification marked as unread");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok("Notification deleted");
    }

    // Endpoint for testing or manual notification triggering
    @PostMapping("/send")
    public ResponseEntity<Notification> sendNotification(
            @RequestParam int senderId,
            @RequestParam int receiverId,
            @RequestParam String category,
            @RequestParam String message,
            @RequestParam(required = false) Integer relatedEntityId) {
        try {
            Notification n = notificationService.sendNotification(senderId, receiverId, category, message,
                    relatedEntityId);
            if (n == null) {
                return ResponseEntity.status(403).body(null); // User unsubscribed
            }
            return ResponseEntity.ok(n);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}
