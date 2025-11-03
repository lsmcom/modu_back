package back.code.filter;

import back.code.common.utils.JWTUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.util.Arrays;

/**
 * JWT 기반 로그아웃 처리 필터.
 *
 * <p>Refresh Token 쿠키를 무효화(삭제)하여
 * 클라이언트 측 세션을 종료시킨다.</p>
 *
 * <p>로그아웃 요청은 POST /api/v1/logout 으로 한정된다.</p>
 */
@RequiredArgsConstructor
public class CustomLogoutFilter extends GenericFilterBean {

    private final JWTUtils jwtUtils;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        process((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    private void process(HttpServletRequest request,
                         HttpServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        String requestURI = request.getRequestURI();
        String requestMethod = request.getMethod();

        // 로그아웃 요청이 아니면 다음 필터로 넘김
        if (!requestURI.equals("/api/v1/logout") || !"POST".equalsIgnoreCase(requestMethod)) {
            chain.doFilter(request, response);
            return;
        }

        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");

        try {
            Cookie[] cookies = request.getCookies();
            if (cookies == null) {
                throw new IllegalStateException("쿠키 정보가 없습니다.");
            }

            // refresh 쿠키 추출
            String refreshToken = Arrays.stream(cookies)
                    .filter(cookie -> "refresh".equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Refresh Token이 존재하지 않습니다."));

            // 토큰 유효성 검증
            if (jwtUtils.isExpired(refreshToken)) {
                throw new IllegalStateException("Refresh Token이 만료되었습니다.");
            }

            String category = jwtUtils.getCategory(refreshToken);
            if (!"refresh".equals(category)) {
                throw new IllegalStateException("Refresh Token이 아닙니다.");
            }

            // Refresh Token 쿠키 삭제
            Cookie cookie = new Cookie("refresh", null);
            cookie.setMaxAge(0);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            response.addCookie(cookie);

            // 성공 응답
            JSONObject obj = new JSONObject();
            obj.put("resultMsg", "로그아웃 성공");
            obj.put("status", HttpServletResponse.SC_OK);

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(obj.toString());

        } catch (Exception e) {
            e.printStackTrace();

            // 실패 응답
            JSONObject obj = new JSONObject();
            obj.put("resultMsg", e.getMessage() != null ? e.getMessage() : "로그아웃 실패");
            obj.put("status", HttpServletResponse.SC_BAD_REQUEST);

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(obj.toString());
        }
    }
}
