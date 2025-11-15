package back.code.admin.dto.announcement;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 관리자 공지 조회(문의 + 커뮤니티 공지 통합 DTO)
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnnouncementResponseDTO {

    private Long seq;
    private String type;        // "INQUIRY", "COMMUNITY_POST"
    private Long id;            // inquiryId or postId
    private String title;
    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createAt;
}
