package back.code.admin.dto.inquiry;


import back.code.inquiry.dto.InquiryDto;
import back.code.inquiry.entity.InquiryEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminInquiryDTO {

    private Long seq;
    private Long inquiryId;
    private String userId;
    private String userNick;
    private String title;
    private String content;
    private boolean isPublic;
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createAt;

    public static AdminInquiryDTO fromEntity(InquiryEntity inquiry) {
        return AdminInquiryDTO.builder()
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
