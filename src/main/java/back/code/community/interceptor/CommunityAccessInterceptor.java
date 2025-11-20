package back.code.community.interceptor;

import back.code.common.utils.SecurityUtils;
import back.code.user.entity.UserEntity;
import back.code.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class CommunityAccessInterceptor implements HandlerInterceptor {

    private final UserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        // 현재 로그인한 사용자 ID
        String userId = SecurityUtils.getCurrentUserId();

        // 비로그인(guest)은 접근 허용 → 커뮤니티 읽기 가능
        if (userId == null) {
            return true;
        }

        UserEntity user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            return true; // 예외 케이스이므로 그냥 허용
        }

        // 정지(inactive) 사용자 차단
        if ("inactive".equalsIgnoreCase(user.getStatus())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);  // 403
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/plain; charset=UTF-8");
            response.getWriter().write("INACTIVE_USER");
            return false;
        }

        return true;
    }
}
