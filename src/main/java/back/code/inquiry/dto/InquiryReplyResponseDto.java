package back.code.inquiry.dto;

import back.code.inquiry.entity.InquiryReply;
import lombok.*;

import java.time.LocalDateTime;

// 4. 코드 내 수정된 부분을 명확히 표시: 답변 응답용 DTO (repliedAt -> createAt)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InquiryReplyResponseDto {
    private Long replyId;
    private String userId;
    private String content;
    private LocalDateTime repliedAt; // 4. 코드 내 수정된 부분을 명확히 표시: 필드명은 유지 (프론트 통신)

    public static InquiryReplyResponseDto fromEntity(InquiryReply reply) {
        return InquiryReplyResponseDto.builder()
                .replyId(reply.getReplyId())
                .userId(reply.getAdmin().getUserId())
                .content(reply.getContent())
                .repliedAt(reply.getCreateAt()) // 4. 코드 내 수정된 부분을 명확히 표시: reply.getRepliedAt() 대신 reply.getCreateAt() 사용
                .build();
    }
}