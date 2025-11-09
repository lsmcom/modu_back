package back.code.inquiry.dto;

import lombok.*;

// 4. 코드 내 수정된 부분을 명확히 표시: 답변 등록 요청용 DTO
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InquiryReplyRequestDto {
    private String userId;
    private String content;
}