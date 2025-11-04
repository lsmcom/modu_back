package back.code.user.dto;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequest {
    private String userName;
    private String email;
    private LocalDate birth;
    private String agency;
    private String phone;
    private String addr;
    private String addrDetail;
}
