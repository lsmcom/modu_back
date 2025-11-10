package back.code.inquiry.entity;

import back.code.common.entity.BaseTimeEntity; // 4. 코드 내 수정된 부분을 명확히 표시: BaseTimeEntity 상속
import back.code.user.entity.UserEntity; // 4. 코드 내 수정된 부분을 명확히 표시: UserEntity 경로 및 이름 반영
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

import java.util.ArrayList;
import java.util.List;

/**
 * 4. 코드 내 수정된 부분을 명확히 표시: 문의 테이블 엔티티 (BaseTimeEntity 상속)
 */
@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "inquiry")
@DynamicUpdate
public class Inquiry extends BaseTimeEntity { // 4. 코드 내 수정된 부분을 명확히 표시: BaseTimeEntity 상속

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inquiry_id", nullable = false)
    private Long inquiryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user; // 4. 코드 내 수정된 부분을 명확히 표시: User -> UserEntity 로 변경

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    // DB: ENUM('temp', 'submitted', 'answered')
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InquiryStatus status;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic;

    @Transient
    private String type;

    @Transient
    private String fileName;

    // 답변 목록
    @OneToMany(mappedBy = "inquiry", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InquiryReply> replies = new ArrayList<>();


    // 4. 코드 내 수정된 부분을 명확히 표시: createAt/updateAt 자동 Auditing으로 인해 상태/공개 여부만 초기화
    @PrePersist
    public void prePersist() {
        if (this.status == null) {
            this.status = InquiryStatus.SUBMITTED;
        }
        if (this.isPublic == null) {
            this.isPublic = false;
        }
    }
}