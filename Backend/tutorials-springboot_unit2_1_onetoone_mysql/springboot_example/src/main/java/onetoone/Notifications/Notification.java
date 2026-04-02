package onetoone.Notifications;

import jakarta.persistence.*;
import onetoone.Users.User;
import java.time.LocalDateTime;

/**
 * @author Matthew Smith
 */
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private NotificationCategory category;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = false)
    private boolean isRead = false;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column
    private Integer relatedEntityId;

    public Notification() {
        this.timestamp = LocalDateTime.now();
    }

    public Notification(User user, NotificationCategory category, String message, Integer relatedEntityId) {
        this.user = user;
        this.category = category;
        this.message = message;
        this.relatedEntityId = relatedEntityId;
        this.isRead = false;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public NotificationCategory getCategory() {
        return category;
    }

    public void setCategory(NotificationCategory category) {
        this.category = category;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getRelatedEntityId() {
        return relatedEntityId;
    }

    public void setRelatedEntityId(Integer relatedEntityId) {
        this.relatedEntityId = relatedEntityId;
    }
}
