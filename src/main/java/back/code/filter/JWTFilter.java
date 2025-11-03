package back.code.filter;

import back.code.common.utils.JWTUtils;
import back.code.security.dto.SecureUserDTO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * JWT 인증 필터
 *
 * <p>Spring Security 필터 체인에서 로그인 이후 요청이 들어올 때,
 * 요청 헤더의 JWT 토큰을 검증하고, 유효하면 인증 정보를 SecurityContext에 저장한다.</p>
 *
 * <p>이 필터는 {@link OncePerRequestFilter}를 상속받아
 * 요청당 한 번만 실행된다.</p>
 */
@RequiredArgsConstructor
@Slf4j
public class JWTFilter  extends OncePerRequestFilter{

    private final JWTUtils jwtUtils;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Authorization 헤더에서 토큰 추출
        String accessToken = request.getHeader("Authorization");

        // 토큰이 없으면 다음 필터로 넘김 (비로그인 접근 허용)
        if(accessToken == null) {
            log.info("acessToken is null");
            filterChain.doFilter(request, response);
            return;
        }

        try{
            // Bearer 접두어 제거
            if(accessToken.startsWith("Bearer ")) {
                accessToken = accessToken.substring(7);

                // 토큰 유효성 검증
                if( !jwtUtils.validateToken(accessToken)) {
                    throw new IllegalAccessException("유효하지 않은 토큰입니다.");
                }
            }else {
                throw new IllegalAccessException("토큰 형식이 잘못되었습니다.");
            }

            // 토큰 카테고리(access / refresh) 검증
            String category = jwtUtils.getCategory(accessToken);

            if(! category.equals("access")) {
                throw new IllegalAccessException("유효하지 않은 토큰입니다.");
            }

        }catch(Exception e) {
            // 토큰 검증 실패 시 JSON 형태로 에러 응답 반환
            log.warn("JWT 검증 실패: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json; charset=UTF-8");

            JSONObject message = getErrorMessage(e.getMessage(), HttpServletResponse.SC_UNAUTHORIZED);
            try (PrintWriter writer = response.getWriter()) {
                writer.write(message.toString());
            }
            return;
        }

        // 검증 성공 시 사용자 정보 추출
        String userId = jwtUtils.getUserId(accessToken);
        String userName = jwtUtils.getUserName(accessToken);
        String userRole = jwtUtils.getUserRole(accessToken);

        // 사용자 정보를 SecurityContext에 등록
        SecureUserDTO dto = new SecureUserDTO(userId, userName, userName, userRole);

        // 시큐리티 세션에 저장()
        Authentication authentication = 
                new UsernamePasswordAuthenticationToken(dto, null, dto.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
        
    }

    /**
     * 에러 응답용 JSON 객체 생성
     *
     * @param message 에러 메시지
     * @param status  HTTP 상태 코드
     * @return JSONObject 형태의 에러 메시지
     */
    private JSONObject  getErrorMessage(String message, int status) {
        JSONObject jObj = new JSONObject();
        jObj.put("resultMsg", message == null ? "Invalid Token" : message);
        jObj.put("status", status);
        return jObj;
    }
}
