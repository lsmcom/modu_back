package back.code.admin.dto.user;

import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAllInfoDTO {

    private String userId;
    private String userName;
    private String userNick;
    private String email;
    private LocalDate birth;
    private String agency;
    private String phone;
    private String addr;
    private String addrDetail;

    private LocalDateTime createAt;
    private LocalDateTime updateAt;
    private Instant withdrawAt;

    private String withdrawReason;
    private String socialType;
    private String status;
    private String roleId;  // USER, ADMIN 등
}
