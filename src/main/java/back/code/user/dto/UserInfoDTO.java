package back.code.user.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfoDTO {
    private String userId;
    private String userName;
    private String email;
    private LocalDate birth;
    private String agency;
    private String phone;
    private String addr;
    private String addrDetail;
    private LocalDateTime createAt;
}