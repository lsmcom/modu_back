package back.code.community.entity;

import back.code.common.entity.BaseCreateTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "community_report")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunityReportEntity extends BaseCreateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer reportId; // 신고 번호

    private Integer postId; // 신고된 게시글 번호

    private String userId; // 신고한 회원 아이디

    private String reportReason; // 신고 사유

    @Enumerated(EnumType.STRING)
    @Column(name = "report_status", nullable = false)
    @Builder.Default
    private ReportStatus reportStatus = ReportStatus.PENDING; // 신고 처리 상태, 자동으로 PENDING 지정

    public enum ReportStatus {
        PENDING, APPROVED, REJECTED
    }
}
