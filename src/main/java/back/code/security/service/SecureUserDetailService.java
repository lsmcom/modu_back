package back.code.security.service;

import back.code.security.dto.SecureUserDTO;
import back.code.user.dto.LoginUserInfoDTO;
import back.code.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Security 인증 과정에서 사용자 정보를 로드하는 서비스 클래스.
 *
 * <p>로그인 시 사용자 ID(username)를 기준으로 DB에서
 * {@link LoginUserInfoDTO}를 사용하여 최소한의 컬럼만 SELECT하여 성능을 최적화하고
 * {@link SecureUserDTO}로 변환하여 Spring Security 인증 객체로 반환한다.</p>
 */
@Service
@RequiredArgsConstructor
public class SecureUserDetailService implements  UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // DTO Projection으로 최소 데이터만 조회
        LoginUserInfoDTO user = userRepository.findLoginInfo(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자 없음"));

        // 탈퇴 회원 로그인 차단
        if ("withdrawn".equalsIgnoreCase(user.getStatus())) {
            throw new UsernameNotFoundException("탈퇴한 회원은 로그인할 수 없습니다.");
        }

        // 필요 시 추가 검증
        if ("inactive".equalsIgnoreCase(user.getStatus())) {
            throw new UsernameNotFoundException("비활성화된 계정입니다.");
        }

        // DB의 사용자 정보를 기반으로 인증용 DTO 생성
        return new SecureUserDTO(user.getUserId(), user.getUserName(),
                user.getPassword(), user.getRoleName()) ;
    }
}
