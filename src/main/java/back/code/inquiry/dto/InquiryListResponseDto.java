package back.code.inquiry.dto;

import back.code.inquiry.entity.Inquiry; // 4. 코드 내 수정된 부분을 명확히 표시: 파일 경로 수정
import back.code.inquiry.entity.InquiryStatus; // 4. 코드 내 수정된 부분을 명확히 표시: 파일 경로 수정
import lombok.*;

import java.time.LocalDateTime;

// 4. 코드 내 수정된 부분을 명확히 표시: 목록 조회용 DTO
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InquiryListResponseDto {
    private Long inquiryId;
    private String userId;
    private String title;
    private LocalDateTime createAt;
    private Boolean isPublic;
    private InquiryStatus status;
    private String type;
    private Long replyCount;

    public static InquiryListResponseDto fromEntity(Inquiry inquiry, String userType, Long replyCount) {
        return InquiryListResponseDto.builder()
                .inquiryId(inquiry.getInquiryId())
                .userId(inquiry.getUser().getUserId())
                .title(inquiry.getTitle())
                .createAt(inquiry.getCreateAt())
                .isPublic(inquiry.getIsPublic())
                .status(inquiry.getStatus())
                .type(userType)
                .replyCount(replyCount)
                .build();
    }
}