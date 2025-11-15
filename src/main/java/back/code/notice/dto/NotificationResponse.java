package back.code.notice.dto;

import back.code.notice.entity.Notification;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class NotificationResponse {

    private Long notificationId;
    private String userId;
    private String senderId;
    private Long milestoneId;
    private Long inquiryId;
    private String type;
    private String title;
    private String content;
    private Boolean isRead;
    private LocalDateTime createDate;

    /**
     * Notification 엔티티를 NotificationResponse DTO로 변환합니다.
     */
    public static NotificationResponse fromEntity(Notification notification) {
        return NotificationResponse.builder()
                .notificationId(notification.getNotificationId())
                .userId(notification.getUserId())
                .senderId(notification.getSenderId())
                .milestoneId(notification.getMilestoneId())
                .inquiryId(notification.getInquiryId())
                // Enum 타입을 String으로 변환하여 전달
                .type(notification.getType() != null ? notification.getType().name() : null)
                .title(notification.getTitle())
                .content(notification.getContent())
                .isRead(notification.getIsRead())
                .createDate(notification.getCreateDate())
                .build();
    }
}