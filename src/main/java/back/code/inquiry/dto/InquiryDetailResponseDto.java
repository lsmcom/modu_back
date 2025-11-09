package back.code.inquiry.dto;

import back.code.inquiry.entity.Inquiry; // 4. 코드 내 수정된 부분을 명확히 표시: 파일 경로 수정
import back.code.inquiry.entity.InquiryStatus; // 4. 코드 내 수정된 부분을 명확히 표시: 파일 경로 수정
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

// 4. 코드 내 수정된 부분을 명확히 표시: 상세 조회용 DTO
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InquiryDetailResponseDto {
    private Long inquiryId;
    private String userId;
    private String title;
    private String content;
    private String fileName;
    private LocalDateTime createDate;
    private Boolean isPublic;
    private InquiryStatus status;
    private Long replyCount;
    private List<InquiryReplyResponseDto> replies;

    public static InquiryDetailResponseDto fromEntity(Inquiry inquiry, Long replyCount, List<InquiryReplyResponseDto> replies) {
        return InquiryDetailResponseDto.builder()
                .inquiryId(inquiry.getInquiryId())
                .userId(inquiry.getUser().getUserId())
                .title(inquiry.getTitle())
                .content(inquiry.getContent())
                .fileName(inquiry.getFileName())
                .createDate(inquiry.getCreateAt()) // createAt 사용
                .isPublic(inquiry.getIsPublic())
                .status(inquiry.getStatus())
                .replyCount(replyCount)
                .replies(replies)
                .build();
    }
}