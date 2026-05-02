package onetoone.ImageHandler;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;


/**
 * @author Chris Lopez
 */
@Repository
public interface ImageMessageRepository extends JpaRepository<ImageMessage, Long> {


        List<ImageMessage> findByChannelId(Long channelId);

        List<ImageMessage> findByAuthorId(Long authorId);

        List<ImageMessage> findByChannelIdOrderByDispatchTimeDesc(Long channelId);
    }
