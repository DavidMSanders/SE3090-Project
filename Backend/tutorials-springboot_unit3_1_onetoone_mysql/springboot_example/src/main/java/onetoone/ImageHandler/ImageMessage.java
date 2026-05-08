package onetoone.ImageHandler;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "image_messages")
public class ImageMessage {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id")
        private Long id;

        @Column(name = "sender_id", nullable = false)
        private Long senderId;

        @Column(name = "group_id") // FIXED: removed nullable=false, groupId can be null
        private Long groupId;

        @Column(name = "image_url", nullable = false)
        private String imageUrl;

        @Column(name = "caption")
        private String caption;

        @Column(name = "sent_at")
        private LocalDateTime sentAt;

        public ImageMessage() {}

        public ImageMessage(Long senderId, Long groupId, String imageUrl, String caption) {
            this.senderId = senderId;
            this.groupId = groupId;
            this.imageUrl = imageUrl;
            this.caption = caption;
            this.sentAt = LocalDateTime.now();
    }


        public Long getId() {

            return id;
        }
        public void setId(Long id) {

            this.id = id;
        }

        public Long getSenderId() {
        return senderId;
    }

        public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

        public Long getGroupId() {
        return groupId;
    }

        public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

        public String getImageUrl() {
        return imageUrl;
    }

        public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

        public String getCaption() {
        return caption;
    }

        public void setCaption(String caption) {
        this.caption = caption;
    }

        public LocalDateTime getSentAt() {
        return sentAt;
    }

        public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

        @Override
        public String toString() {
            return "ImageMessage{" +
                    "id=" + id +
                    ", senderId=" + senderId +
                    ", groupId=" + groupId +
                    ", imageUrl='" + imageUrl + '\'' +
                    ", sentAt=" + sentAt +
                    '}';
        }
    }

