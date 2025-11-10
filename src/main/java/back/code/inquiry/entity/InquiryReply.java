package back.code.inquiry.entity;

import back.code.common.entity.BaseTimeEntity;
import back.code.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

/**
 * 4. 코드 내 수정된 부분을 명확히 표시: 문의 답변 테이블 엔티티 (repliedAt -> createAt으로 통합)
 */
@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "inquiry_reply")
@DynamicUpdate
public class InquiryReply extends BaseTimeEntity { // BaseTimeEntity가 createAt과 updateAt을 제공

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reply_id", nullable = false)
    private Long replyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquiry_id", nullable = false)
    private Inquiry inquiry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private UserEntity admin;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    // 4. 코드 내 수정된 부분을 명확히 표시: repliedAt 필드 제거 (BaseTimeEntity의 createAt으로 대체)


    // 4. 코드 내 수정된 부분을 명확히 표시: repliedAt 제거로 prePersist 로직 삭제
    /* @PrePersist
    public void prePersist() {
        // this.repliedAt = LocalDateTime.now();
    } */
}