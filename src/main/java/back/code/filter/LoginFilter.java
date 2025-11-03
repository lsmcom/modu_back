package back.code.filter;

import back.code.common.utils.CookieUtils;
import back.code.common.utils.JWTUtils;
import back.code.security.dto.SecureUserDTO;
import back.code.user.dto.LoginRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Iterator;

/**
 * 로그인 요청을 처리하는 Spring Security 커스텀 필터.
 *
 * <p>{@link UsernamePasswordAuthenticationFilter}를 상속받아
 * username/password 기반 인증 시 JWT를 발급한다.</p>
 */
@RequiredArgsConstructor
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JWTUtils jwtUtils;
    private final CookieUtils cookieUtils;

    /** Access Token 만료 시간 (분 단위) */
    public static final long ACCESS_TOKEN_EXPIRE_TIME = 1440L; // 24시간

    /** Refresh Token 만료 시간 (분 단위) */
    public static final long REFRESH_TOKEN_EXPIRE_TIME = 1440L; // 24시간

    /**
     * 로그인 요청 시 인증 시도.
     *
     * <p>Spring Security가 자동으로 username, password 파라미터를 매핑한다.</p>
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        try {
            // JSON 요청 본문을 DTO로 변환
            ObjectMapper objectMapper = new ObjectMapper();
            LoginRequestDTO loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequestDTO.class);

            String username = loginRequest.getUserId(); // userId를 Security의 username으로 사용
            String password = loginRequest.getPassword();

            UsernamePasswordAuthenticationToken authRequest =
                    new UsernamePasswordAuthenticationToken(username, password);

            // AuthenticationManager에게 인증 요청
            return authenticationManager.authenticate(authRequest);
        } catch (IOException e) {
            throw new RuntimeException("로그인 요청 JSON 파싱 실패", e);
        }
    }

    /**
     * 인증 성공 시 JWT 생성 및 응답 처리.
     *
     * @param authResult 인증 결과 객체
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {

        SecureUserDTO user = (SecureUserDTO) authResult.getPrincipal();
        String userId = user.getUserId();
        String userName = user.getUserName();

        // 현재 권한은 1개만 존재한다고 가정
        Iterator<? extends GrantedAuthority> iter = authResult.getAuthorities().iterator();
        String userRole = iter.hasNext() ? iter.next().getAuthority() : "ROLE_USER";

        // JWT 생성
        String accessToken = jwtUtils.createToken("access", userId, userName, userRole, ACCESS_TOKEN_EXPIRE_TIME);
        String refreshToken = jwtUtils.createToken("refresh", userId, userName, userRole, REFRESH_TOKEN_EXPIRE_TIME);

        // Refresh Token 쿠키 저장
        Cookie cookie = cookieUtils.createCookie("refresh", refreshToken, (int) REFRESH_TOKEN_EXPIRE_TIME);
        response.addCookie(cookie);

        // 응답 헤더 및 본문 설정
        response.setHeader("Authorization", accessToken);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);

        // JSON 응답 구성
        JSONObject jObj = new JSONObject();
        jObj.put("resultMsg", "OK");
        jObj.put("status", 200);

        JSONObject data = new JSONObject();
        data.put("userId", userId);
        data.put("userName", userName);
        data.put("userRole", userRole);
        data.put("token", accessToken);

        jObj.put("content", data);
        response.getWriter().write(jObj.toString());
    }

    /**
     * 인증 실패 시 응답 처리.
     */
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) throws IOException {

        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        JSONObject jObj = new JSONObject();
        jObj.put("resultMsg", "FAIL");
        jObj.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        jObj.put("content", new JSONObject());

        response.getWriter().write(jObj.toString());
    }
}
