package back.code.config;

import back.code.common.utils.CookieUtils;
import back.code.common.utils.JWTUtils;
import back.code.filter.CustomLogoutFilter;
import back.code.filter.JWTFilter;
import back.code.filter.LoginFilter;
import back.code.security.service.SecureUserDetailService;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Spring Security의 전역 보안 설정을 담당하는 클래스.
 *
 * <p>JWT 기반의 인증/인가 구조로 세션을 사용하지 않는다.</p>
 * <p>LoginFilter, JWTFilter, CustomLogoutFilter를 순차적으로 구성하여
 * 토큰 발급 → 검증 → 로그아웃 처리를 수행한다.</p>
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final SecureUserDetailService serviceDetails;
    private final JWTUtils jwtUtils;
    private final CookieUtils cookieUtils;

    /**
     * 정적 리소스에 대한 시큐리티 필터 무시 설정.
     * (이미지, CSS, JS 등은 인증 없이 접근 가능)
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web ->
                web.ignoring()
                        .requestMatchers("/static/imgs/**")
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

    /**
     * HTTP 보안 설정
     *
     * <p>JWT 기반 인증이므로 세션은 STATELESS 모드로 동작.</p>
     * <p>Form 로그인, HTTP Basic은 비활성화하고, 커스텀 필터 기반 인증으로 처리.</p>
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        AuthenticationConfiguration configuration = http.getSharedObject(AuthenticationConfiguration.class);
        AuthenticationManager manager = this.authenticationManager(configuration);

        // 커스텀 로그인 필터 등록
        LoginFilter loginFilter = new LoginFilter(manager, jwtUtils, cookieUtils);
        loginFilter.setFilterProcessesUrl("/api/v1/login");

        http
                // CSRF 비활성화 (JWT 환경에서는 필요 없음)
                .csrf(AbstractHttpConfigurer::disable)

                // CORS 설정 적용
                .cors(cors -> cors.configurationSource(this.configurationSource()))

                // 폼 로그인 / HTTP Basic 비활성화
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)

                // 인가(Authorization) 규칙 정의
                .authorizeHttpRequests(auth ->
                        auth.dispatcherTypeMatchers(DispatcherType.FORWARD).permitAll()
                                .requestMatchers("/api/v1/login/**").permitAll()
                                .requestMatchers("/api/v1/oauth/kakao/**").permitAll()
                                .requestMatchers("/api/v1/oauth/naver/**").permitAll()
                                .requestMatchers("/api/v1/logout/**").permitAll()
                                .requestMatchers("/api/v1/user/verify/**").permitAll()
                                .requestMatchers("/api/v1/user/join/**").permitAll()
                                .requestMatchers("/api/v1/user/check/**").permitAll()
                                .requestMatchers("/api/v1/user/find/**").permitAll()
                                .requestMatchers("/api/v1/user/reset/**").permitAll()
                                .requestMatchers("/api/v1/refresh").permitAll()
                                .requestMatchers("/swagger-ui/**").permitAll()
                                .requestMatchers("/files/**").permitAll()
                                .requestMatchers("/api/v1/file/**").permitAll()
                                .requestMatchers("/v3/api-docs/**").permitAll()
                                .requestMatchers("/admin/**", "/api/v1/admin/**").hasAnyRole("ADMIN")
                                .anyRequest().authenticated()
                )

                // 필터 체인 순서 지정
                // LoginFilter 전에 JWTFilter 실행 → 요청마다 토큰 검증
                .addFilterBefore(new JWTFilter(jwtUtils), LoginFilter.class)
                // UsernamePasswordAuthenticationFilter 대신 LoginFilter 사용
                .addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class)
                // LogoutFilter 앞에 커스텀 로그아웃 필터 실행
                .addFilterBefore(new CustomLogoutFilter(jwtUtils), LogoutFilter.class)

                // 세션 완전 비활성화 (STATELESS)
                .sessionManagement(
                        session -> session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // logout() 기본 설정 유지 (사용하지 않아도 null pointer 방지)
                .logout(withDefaults());

        return http.build();
    }

    /**
     * AuthenticationProvider 설정.
     *
     * <p>Spring Security가 사용자 인증 시 DB 정보를 조회하기 위해
     * UserDetailsService 구현체(SecureUserDetailService)를 사용하도록 연결한다.</p>
     */
    @Bean
    public AuthenticationProvider authProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(serviceDetails);
        provider.setPasswordEncoder(bcyPasswordEncoder());
        return provider;
    }

    /**
     * BCrypt 암호화 방식 설정.
     * <p>비밀번호를 단방향 해시하여 안전하게 저장한다.</p>
     */
    @Bean
    public PasswordEncoder bcyPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationManager 설정.
     * <p>LoginFilter에서 사용자 인증을 처리할 때 필요.</p>
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * CORS(Cross-Origin Resource Sharing) 설정.
     * <p>React, Vue 등의 프론트엔드 도메인에서 API 접근을 허용한다.</p>
     */
    @Bean
    public CorsConfigurationSource configurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 허용 헤더
        config.setAllowedHeaders(List.of(
                "Authorization", "Content-Type", "X-Requested-With", "Accept",
                "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"
        ));

        // 허용 메서드
        config.setAllowedMethods(List.of("GET", "POST", "DELETE", "PUT", "PATCH", "OPTIONS"));

        // 허용 Origin (프론트 개발용)
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:3001",
                "http://localhost:4000",
                "http://localhost:4001",
                "http://localhost:4002"
        ));

        // 쿠키 포함 허용
        config.setAllowCredentials(true);

        // Preflight 캐싱 시간 (1시간)
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
