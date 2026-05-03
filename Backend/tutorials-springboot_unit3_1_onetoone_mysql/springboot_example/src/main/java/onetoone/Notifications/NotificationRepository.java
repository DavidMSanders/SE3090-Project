package onetoone.Notifications;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Matthew Smith
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByTimestampDesc(int userId);

    List<Notification> findByUserIdAndIsReadOrderByTimestampDesc(int userId, boolean isRead);
}
