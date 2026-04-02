package onetoone.Notifications;

import jakarta.persistence.*;
import onetoone.Users.User;

/**
 * @author Matthew Smith
 */
@Entity
@Table(name = "notification_preferences", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user_id", "category_id" })
})
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private NotificationCategory category;

    @Column(nullable = false)
    private boolean isSubscribed;

    public NotificationPreference() {
    }

    public NotificationPreference(User user, NotificationCategory category, boolean isSubscribed) {
        this.user = user;
        this.category = category;
        this.isSubscribed = isSubscribed;
    }

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

    public boolean isSubscribed() {
        return isSubscribed;
    }

    public void setSubscribed(boolean subscribed) {
        isSubscribed = subscribed;
    }
}
