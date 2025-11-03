package back.code.common.utils;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SecurityException;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * JWT 토큰 생성 및 검증을 담당하는 유틸리티 클래스.
 *
 * <p>HS256 알고리즘을 기반으로 JWT를 서명 및 검증하며,</p>
 * <p>사용자 ID, 이름, 권한 등의 Claim을 포함할 수 있다.</p>
 *
 * <p> 주의사항:
 * secretKey는 반드시 32자(256bit) 이상으로 설정해야 하며,
 * 운영 환경에서는 환경 변수나 Secret Manager에 보관해야 한다.
 * </p>
 */
@Slf4j
@Component
public class JWTUtils {

    /** 비밀 키 (서명 및 검증용) */
    private final SecretKey secretKey;

    /**
     * 생성자: application.yml에 정의된 비밀키를 이용해 HS256 서명 키를 생성한다.
     *
     * @param secret application.yml 내의 spring.jwt.secretKey 값
     */
    public JWTUtils(@Value("${spring.jwt.secretKey}") String secret) {
        this.secretKey = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS256.key().build().getAlgorithm()
        );
    }

    /**
     * JWT 토큰 생성
     *
     * @param category 토큰 카테고리 (access / refresh 등)
     * @param userId   사용자 ID
     * @param userName 사용자 이름
     * @param userRole 사용자 권한
     * @param minutes  토큰 유효시간(분)
     * @return 서명된 JWT 문자열
     */
    public String createToken(String category, String userId, String userName, String userRole, long minutes) {
        Date issuedAt = new Date();
        Date expiration = Date.from(
                LocalDateTime.now()
                        .plusMinutes(minutes)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
        );

        return Jwts.builder()
                .claim("category", category)
                .claim("userId", userId)
                .claim("userName", userName)
                .claim("userRole", userRole)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    /**
     * JWT 토큰의 유효성을 검증한다.
     *
     * <p>서명 무결성, 형식, 만료 여부 등을 확인하며,
     * 유효한 경우 true, 잘못된 경우 false를 반환한다.</p>
     *
     * @param token 검증할 JWT
     * @return 유효 여부 (true: 정상, false: 비정상)
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰입니다.");
        } catch (SecurityException | MalformedJwtException e) {
            log.error("유효하지 않은 JWT 서명입니다.");
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.error("잘못된 JWT 토큰입니다.");
        }
        return false;
    }

    /**
     * 토큰의 Claim 정보를 파싱한다.
     *
     * @param token JWT 문자열
     * @return Claim 객체
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 토큰에서 카테고리(claim "category")를 추출한다.
     *
     * @param token JWT 문자열
     * @return 카테고리 값
     */
    public String getCategory(String token) {
        return parseClaims(token).get("category", String.class);
    }

    /**
     * 토큰에서 사용자 ID(claim "userId")를 추출한다.
     *
     * @param token JWT 문자열
     * @return 사용자 ID
     */
    public String getUserId(String token) {
        return parseClaims(token).get("userId", String.class);
    }

    /**
     * 토큰에서 사용자 이름(claim "userName")을 추출한다.
     *
     * @param token JWT 문자열
     * @return 사용자 이름
     */
    public String getUserName(String token) {
        return parseClaims(token).get("userName", String.class);
    }

    /**
     * 토큰에서 사용자 권한(claim "userRole")을 추출한다.
     *
     * @param token JWT 문자열
     * @return 사용자 권한
     */
    public String getUserRole(String token) {
        return parseClaims(token).get("userRole", String.class);
    }

    /**
     * 토큰이 만료되었는지 확인한다.
     *
     * @param token JWT 문자열
     * @return true = 이미 만료됨, false = 아직 유효
     */
    public boolean isExpired(String token) {
        Date expiration = parseClaims(token).getExpiration();
        return expiration.before(new Date());
    }
}
