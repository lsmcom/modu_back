package back.code.admin.dto.report;

import back.code.community.entity.CommunityReportEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminReportDTO {

    private Long seq;
    private Integer reportId;      // 신고 번호
    private Integer postId;        // 신고된 게시글 번호
    private String userId;         // 신고한 회원 아이디
    private String reportReason;   // 신고 사유
    private String reportStatus;   // PENDING / APPROVED / REJECTED

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createAt;   // 생성일(BaseCreateTimeEntity 상속)

    /**
     * Entity → DTO 변환 메서드
     */
    public static AdminReportDTO fromEntity(CommunityReportEntity entity) {
        return AdminReportDTO.builder()
                .reportId(entity.getReportId())
                .postId(entity.getPostId())
                .userId(entity.getUserId())
                .reportReason(entity.getReportReason())
                .reportStatus(entity.getReportStatus().name())
                .createAt(entity.getCreatedAt())
                .build();
    }
}
