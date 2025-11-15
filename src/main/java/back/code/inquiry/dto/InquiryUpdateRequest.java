package back.code.inquiry.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InquiryUpdateRequest {
    private Long inquiryId;
    private String title;
    private String content;
    private List<String> keepFileIds;  // 유지할 기존 파일 ID 목록
}

