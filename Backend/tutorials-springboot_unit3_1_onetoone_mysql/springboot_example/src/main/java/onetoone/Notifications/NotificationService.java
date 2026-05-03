package onetoone.Notifications;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import onetoone.Users.User;
import onetoone.Users.UserRepository;
import onetoone.Trainer.TrainerClientRepository;

import java.util.List;
import java.util.Optional;

/**
 * @author Matthew Smith
 */
@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationCategoryRepository categoryRepository;

    @Autowired
    private NotificationPreferenceRepository preferenceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TrainerClientRepository trainerClientRepository;

    public Notification sendNotification(int senderId, int receiverId, String categoryName, String messageText,
            Integer relatedEntityId) {
        // Enforce trainer-client relationship for sensitive categories
        if (categoryName.equalsIgnoreCase("Assignments") || categoryName.equalsIgnoreCase("Feedback")) {
            if (!trainerClientRepository.existsByTrainerIdAndClientId(senderId, receiverId) &&
                    !trainerClientRepository.existsByTrainerIdAndClientId(receiverId, senderId)) {
                throw new RuntimeException("Unauthorized: No trainer-client relationship exists.");
            }
        }

        NotificationCategory category = categoryRepository.findByName(categoryName)
                .orElseGet(
                        () -> categoryRepository.save(new NotificationCategory(categoryName, "Auto-created category")));

        Optional<NotificationPreference> pref = preferenceRepository.findByUserIdAndCategoryId(receiverId,
                category.getId());
        if (pref.isPresent() && !pref.get().isSubscribed()) {
            return null; // User explicitly unsubscribed from this category
        }

        User receiver = userRepository.findById((int) receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        Notification notification = new Notification(receiver, category, messageText, relatedEntityId);
        notificationRepository.save(notification);

        // Attempt to dispatch via websocket real-time
        try {
            NotificationSocket.sendNotificationToUser(receiverId, messageText);
        } catch (Exception e) {
            // User likely not connected, but it's persisted in DB
        }

        return notification;
    }

    public List<Notification> getHistory(int userId) {
        return notificationRepository.findByUserIdOrderByTimestampDesc(userId);
    }

    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    public void markAsUnread(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setRead(false);
            notificationRepository.save(n);
        });
    }

    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }
}
