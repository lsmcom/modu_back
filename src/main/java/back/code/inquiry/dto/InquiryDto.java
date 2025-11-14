package back.code.inquiry.dto;

import back.code.inquiry.entity.InquiryEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Inquiry 조회용 DTO
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InquiryDto {

    private Long inquiryId;
    private String userId;
    private String userNick;
    private String title;
    private String content;
    private boolean isPublic;
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createAt;

    public static InquiryDto fromEntity(InquiryEntity inquiry) {
        return InquiryDto.builder()
                .inquiryId(inquiry.getInquiryId())
                .userId(inquiry.getUser().getUserId())
                .userNick(inquiry.getUser().getUserNick())
                .title(inquiry.getTitle())
                .content(inquiry.getContent())
                .isPublic(inquiry.isPublic())
                .status(inquiry.getStatus().name())
                .createAt(inquiry.getCreateAt())
                .build();
    }
}
