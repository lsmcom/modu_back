package back.code.inquiry.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyInquiryListDTO {
    private Long inquiryId;
    private String title;
    private String status;
}
