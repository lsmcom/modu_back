package back.code.inquiry.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 문의사항 작성/등록 요청 DTO
 * - 파일은 Multipart로 별도 전달, 이 DTO는 메타 데이터만 담당
 */
@Getter
@Setter
public class InquiryCreateRequest {

    private Long inquiryId; // 문의사항 번호

    private String userId; // 문의 작성자 ID

    private String title; // 문의 제목

    private String content; // 문의 내용

    private String status; // 문의 상태
}
