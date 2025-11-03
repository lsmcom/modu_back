package back.code.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 로그인 요청을 받는 DTO
 */
@Getter
@NoArgsConstructor
public class LoginRequestDTO {

    private String userId; //로그인 ID
    private String password; //로그인 PW
}
