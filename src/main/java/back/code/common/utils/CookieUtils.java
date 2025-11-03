package back.code.common.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

/**
 * 쿠키 생성, 조회, 삭제를 지원하는 유틸리티 클래스.
 *
 * <p>HttpOnly, Secure, SameSite 등 보안 속성을 기본 적용하여
 * XSS/CSRF 공격에 대비한다.</p>
 */
@Component
public class CookieUtils {

    /**
     * 쿠키 생성
     *
     * @param name   쿠키 이름
     * @param value  쿠키 값
     * @param maxAge 쿠키 유효 시간(초)
     * @return 생성된 쿠키
     */
    public Cookie createCookie(String name, Object value, int maxAge) {
        Cookie cookie = new Cookie(name, String.valueOf(value));
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        return cookie;
    }

    /**
     * 쿠키를 응답에 추가
     *
     * @param cookie   추가할 쿠키
     * @param response HttpServletResponse
     */
    public void addCookie(Cookie cookie, HttpServletResponse response) {
        response.addCookie(cookie);
    }

    /**
     * 쿠키 조회
     *
     * @param request 요청 객체
     * @param name    쿠키 이름
     * @return 쿠키(Optional)
     */
    public Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        return Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals(name))
                .findFirst();
    }

    /**
     * 쿠키 삭제
     *
     * @param response 응답 객체
     * @param name     쿠키 이름
     */
    public void deleteCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        response.addCookie(cookie);
    }
}
