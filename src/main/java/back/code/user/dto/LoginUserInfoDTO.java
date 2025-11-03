package back.code.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 로그인 시 필요한 최소 사용자 정보만 담는 DTO.
 */
@Getter
@AllArgsConstructor
public class LoginUserInfoDTO {
    private String userId;
    private String userName;
    private String password;
    private String roleName;
}
