package back.code.inquiry.dto;

import back.code.inquiry.entity.Inquiry; // 4. 코드 내 수정된 부분을 명확히 표시: 파일 경로 수정
import back.code.inquiry.entity.InquiryStatus; // 4. 코드 내 수정된 부분을 명확히 표시: 파일 경로 수정
import back.code.user.entity.UserEntity; // 4. 코드 내 수정된 부분을 명확히 표시: UserEntity 경로 및 이름 반영
import lombok.*;

// 4. 코드 내 수정된 부분을 명확히 표시: 문의 등록/수정 요청용 DTO
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InquirySaveRequestDto {
    private String userId;
    private String title;
    private String content;
    private Boolean isPublic;
    private InquiryStatus status;

    public Inquiry toEntity(UserEntity user) { // 4. 코드 내 수정된 부분을 명확히 표시: UserEntity 타입 사용
        return Inquiry.builder()
                .user(user)
                .title(this.title)
                .content(this.content)
                .isPublic(this.isPublic != null ? this.isPublic : false)
                .status(this.status != null ? this.status : InquiryStatus.SUBMITTED)
                .build();
    }
}