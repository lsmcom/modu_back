package back.code.notice.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Getter
@Setter
@NoArgsConstructor
public class Notification {

    /**
     * DB ENUM('공지', '업적', '일정 공유', '문의 답변')에 대응하는 Java Enum 정의
     */
    public enum NotificationType {
        announcement,
        milestone,
        planshare,
        inquiryAnswer
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    // user_id는 FK이지만, 엔티티 관계 설정 없이 String으로 매핑
    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @Column(name = "sender_id")
    private String senderId;   // 알림 보낸 사람 (일정 공유한 userId)

    // FK: Milestone 엔티티 참조
    @Column(name = "milestone_id")
    private Long milestoneId;

    // FK: Inquiry 엔티티 참조
    @Column(name = "inquiry_id")
    private Long inquiryId;

    // 알림 유형 ENUM 매핑
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private NotificationType type;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    // 수정된 DB 스키마에 따라 NULL 허용
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "create_date", nullable = false)
    private LocalDateTime createDate = LocalDateTime.now();

}