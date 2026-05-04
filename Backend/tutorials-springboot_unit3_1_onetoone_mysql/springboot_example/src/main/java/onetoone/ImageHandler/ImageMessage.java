package onetoone.ImageHandler;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class ImageMessage {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long msgKey;

        @Column(name = "author_id", nullable = false)
        private Long authorId;

        @Column(name = "channel_id")
        private Long channelId;

        @Column(name = "media_path", nullable = false)
        private String mediaPath;

        @Column(name = "msg_caption")
        private String msgCaption;

        @Column(name = "dispatch_time")
        private LocalDateTime dispatchTime;

        public ImageMessage() {}

        public ImageMessage(Long authorId, Long channelId, String mediaPath, String msgCaption) {
            this.authorId = authorId;
            this.channelId = channelId;
            this.mediaPath = mediaPath;
            this.msgCaption = msgCaption;
            this.dispatchTime = LocalDateTime.now();
        }

        public Long getMsgKey() {
            return msgKey;
        }
        public void setMsgKey(Long msgKey) {
            this.msgKey = msgKey;
        }

        public Long getAuthorId() {
            return authorId;
        }
        public void setAuthorId(Long authorId) {
            this.authorId = authorId;
        }

        public Long getChannelId() {
            return channelId;
        }
        public void setChannelId(Long channelId) {
            this.channelId = channelId;
        }

        public String getMediaPath() {
            return mediaPath;
        }
        public void setMediaPath(String mediaPath) {
            this.mediaPath = mediaPath;
        }

        public String getMsgCaption() {
            return msgCaption;
        }
        public void setMsgCaption(String msgCaption) {
            this.msgCaption = msgCaption;
        }

        public LocalDateTime getDispatchTime() {
            return dispatchTime;
        }
        public void setDispatchTime(LocalDateTime dispatchTime) {

            this.dispatchTime = dispatchTime;
        }

        @Override
        public String toString() {
            return "ImageMessage{" +
                    "msgKey=" + msgKey +
                    ", authorId=" + authorId +
                    ", channelId=" + channelId +
                    ", mediaPath='" + mediaPath + '\'' +
                    ", dispatchTime=" + dispatchTime +
                    '}';
        }
    }

