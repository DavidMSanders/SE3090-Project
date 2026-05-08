package onetoone.ImageHandler;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;


/**
 * @author Chris Lopez
 */
@Repository
public interface ImageMessageRepository extends JpaRepository<ImageMessage, Long> {


        List<ImageMessage> findByGroupId(Long groupId);

        List<ImageMessage> findBySenderId(Long senderId);

        List<ImageMessage> findByGroupIdOrderBySentAtDesc(Long groupId);
    }
