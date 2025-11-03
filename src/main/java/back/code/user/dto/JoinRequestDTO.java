package back.code.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

/**
 * 회원가입 요청을 받는 DTO
 */
@Getter
@NoArgsConstructor
public class JoinRequestDTO {

    @NotBlank(message = "아이디를 입력하세요.")
    private String userId; //회원 아이디

    @NotBlank(message = "비밀번호를 입력하세요.")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상 입력해야합니다.")
    private String password; //비밀번호

    @NotBlank(message = "이름을 입력하세요.")
    @Length(min = 2, max = 20, message = "이름은 2~20자 이내로 입력하세요.")
    private String userName; //회원 이름

    @NotBlank(message = "닉네임을 입력하세요.")
    @Length(min = 2, max = 20, message = "닉네임은 2~20자 이내로 입력하세요.")
    private String userNick; //회원 닉네임

    @NotBlank(message = "이메일을 입력하세요.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email; //회원 이메일

    @NotBlank(message = "생년월일을 입력하세요.")
    @Pattern(regexp = "\\d{6}", message = "생년월일은 6자리(예: 990123) 형식이어야 합니다.")
    private String birth; //회원 생년월일

    @NotBlank(message = "통신사를 선택하세요.")
    private String agency; //회원 통신사

    @NotBlank(message = "전화번호를 입력하세요.")
    @Pattern(regexp = "^01[0-9]{8,9}$", message = "전화번호 형식이 올바르지 않습니다. (예: 01012345678)")
    private String phone; //회원 전화번호

    @NotBlank(message = "주소를 입력하세요.")
    private String addr; //회원 주소

    @NotBlank(message = "상세주소를 입력하세요.")
    private String addrDetail; //회원 상세주소
}
