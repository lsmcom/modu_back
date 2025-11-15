package back.code.common.utils;

import back.code.security.dto.SecureUserDTO;
import back.code.user.dto.LoginUserInfoDTO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    /** 현재 로그인한 사용자의 userId 반환 */
    public static String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        // Spring Security 기본 User 타입
        if (principal instanceof org.springframework.security.core.userdetails.User user) {
            return user.getUsername();
        }

        // JWTFilter에서 넣은 SecureUserDTO 처리
        if (principal instanceof SecureUserDTO user) {
            return user.getUserId();
        }

        // 기존 LoginUserInfoDTO 처리
        if (principal instanceof LoginUserInfoDTO user) {
            return user.getUserId();
        }

        return null;
    }

    public static String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)  // 예: ROLE_ADMIN
                .findFirst()
                .orElse(null);
    }
}
