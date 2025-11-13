package back.code.security.dto;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Spring Security에서 사용할 인증 사용자 정보 DTO.
 *
 * <p>{@link User}를 상속하여 Security가 요구하는 형태(username, password, authorities)를 그대로 제공하며,
 * 추가로 비즈니스에서 사용하는 사용자 식별자/이름 필드를 노출한다.</p>
 *
 * <p>주의: 이 객체는 인증(Session/SecurityContext)용으로만 사용되며,
 * 비밀번호는 평문이 아닌 암호화된 값이어야 한다.</p>
 */
public class SecureUserDTO extends User {

    /** Spring Security 권한 접두사 규칙 */
    private static final String ROLE_PREFIX = "ROLE_";

    @Getter
    private String userId;
    @Getter
    private String userName;

    /**
     * 인증용 사용자 객체 생성자.
     *
     * @param userId   로그인 ID (Security의 username으로 매핑됨)
     * @param userName 사용자 표시 이름 (프로필/로그용)
     * @param password 암호화된 비밀번호 (BCrypt 등) — JWT 컨텍스트에선 placeholder 사용 가능
     * @param roleId 권한 명(예: USER, ADMIN). 내부에서 {@code ROLE_} 접두사를 자동 부여
     */
    public SecureUserDTO(String userId, String userName, String password, String roleId) {
        super(userId, password, getAuthority(roleId));

        this.userId = userId;
        this.userName = userName;
    }

    /**
     * 단일 권한 문자열을 Spring Security의 권한 컬렉션으로 변환한다.
     *
     * <p>예: "USER" → "ROLE_USER"</p>
     *
     * @param roleId 권한 명(접두사 미포함)
     * @return GrantedAuthority 목록
     */
    private static  List<GrantedAuthority>  getAuthority(String roleId){
        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new SimpleGrantedAuthority(ROLE_PREFIX + roleId));
        return list;
    }
}
