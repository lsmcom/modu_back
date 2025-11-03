package back.code.security.controller;

import back.code.common.dto.ApiResponse;
import back.code.common.utils.CookieUtils;
import back.code.common.utils.JWTUtils;
import back.code.filter.LoginFilter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * JWT Refresh Token 재발급 컨트롤러.
 *
 * <p>쿠키에 저장된 Refresh Token을 검증하여,
 * 유효할 경우 Access Token과 Refresh Token을 모두 재발급한다.</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class RefreshTokenController {

    private final JWTUtils jwtUtils;
    private final CookieUtils cookieUtils;

    /**
     * Refresh Token을 이용한 Access Token 재발급 엔드포인트.
     *
     * @param request  HTTP 요청 객체 (쿠키 포함)
     * @param response HTTP 응답 객체 (쿠키 재등록)
     * @return 새 Access Token 정보
     * @throws Exception 토큰 검증 실패 등 오류 발생 시
     */
    @GetMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request,
                                          HttpServletResponse response) throws Exception {

        // 쿠키 조회 (없을 경우 NPE 방지)
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return ResponseEntity.badRequest().body("No cookies found in request");
        }

        String refreshToken = null;
        for (Cookie cookie : cookies) {
            if ("refresh".equals(cookie.getName())) {
                refreshToken = cookie.getValue();
                break;
            }
        }

        // Refresh Token 유효성 검사
        if (refreshToken == null || !jwtUtils.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpServletResponse.SC_BAD_REQUEST)
                    .body("Invalid refresh token");
        }

        // Refresh Token에서 사용자 정보 추출
        String userId = jwtUtils.getUserId(refreshToken);
        String userName = jwtUtils.getUserName(refreshToken);
        String userRole = jwtUtils.getUserRole(refreshToken);

        // 새 Access/Refresh Token 발급
        String accessToken = jwtUtils.createToken(
                "access", userId, userName, userRole, LoginFilter.ACCESS_TOKEN_EXPIRE_TIME
        );
        String newRefresh = jwtUtils.createToken(
                "refresh", userId, userName, userRole, LoginFilter.REFRESH_TOKEN_EXPIRE_TIME
        );

        // Refresh Token 쿠키 갱신 (덮어쓰기)
        Cookie cookie = cookieUtils.createCookie(
                "refresh", newRefresh, (int) LoginFilter.REFRESH_TOKEN_EXPIRE_TIME
        );
        response.addCookie(cookie);

        // 응답 데이터 구성
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("userName", userName);
        data.put("userRole", userRole);
        data.put("accessToken", accessToken);

        return ResponseEntity.ok(ApiResponse.ok(data));
    }
}
