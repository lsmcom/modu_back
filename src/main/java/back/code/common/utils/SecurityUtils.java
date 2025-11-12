package back.code.common.utils;

import back.code.user.dto.LoginUserInfoDTO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    /** 현재 로그인한 사용자의 userId 반환 */
    public static String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null; // 로그인 안 한 사용자
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.User user) {
            return user.getUsername(); // 일반적인 경우 username이 userId로 사용됨
        }

        // JWT 토큰 기반 커스텀 principal 객체일 경우
        if (principal instanceof LoginUserInfoDTO user) {
            return user.getUserId();
        }

        return null;
    }
}
