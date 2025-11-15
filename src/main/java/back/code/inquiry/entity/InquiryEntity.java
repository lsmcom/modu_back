package back.code.inquiry.entity;

import back.code.common.entity.BaseTimeEntity;
import back.code.user.entity.UserEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Inquiry (문의사항) 엔티티
 */
@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "inquiry")
public class InquiryEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inquiry_id")
    private Long inquiryId;

    // 작성자 정보 (user 테이블 FK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    // 문의 제목
    @Column(nullable = false, length = 255)
    private String title;

    // 문의 내용
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // 문의 상태 (temp / submitted / answered)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InquiryStatus status;

    // 공개 여부 (관리자 공지/FAQ = true)
    @Column(name = "is_public", nullable = false)
    private boolean isPublic;

    @JsonIgnore
    @OneToMany(mappedBy = "inquiry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InquiryFileMappingEntity> files = new ArrayList<>();

    @OneToMany(mappedBy = "inquiry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InquiryReplyEntity> replies = new ArrayList<>();
}
