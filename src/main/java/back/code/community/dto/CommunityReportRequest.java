package back.code.community.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommunityReportRequest {
    private Integer postId;
    private String userId;
    private String reportReason;
}
