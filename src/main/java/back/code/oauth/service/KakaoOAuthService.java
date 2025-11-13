package back.code.oauth.service;

import back.code.common.utils.JWTUtils;
import back.code.file.repository.FileRepository;
import back.code.file.service.FileService;
import back.code.user.entity.UserEntity;
import back.code.user.entity.UserRoleEntity;
import back.code.user.repository.UserRepository;
import back.code.user.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoOAuthService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final JWTUtils jwtUtils;
    private final FileRepository fileRepository;
    private final FileService fileService;

    private final WebClient webClient = WebClient.create();

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.client-secret}")
    private String clientSecret;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    /**
     * 인가 코드로 Access Token 요청
     */
    public String getAccessToken(String code) {
        String tokenUrl = "https://kauth.kakao.com/oauth/token";

        log.info("[KAKAO] 토큰 요청 시작");

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("client_id", clientId);
        formData.add("redirect_uri", redirectUri);
        formData.add("code", code);
        formData.add("client_secret", clientSecret);

        Map<String, Object> response = webClient.post()
                .uri(tokenUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        log.info("[KAKAO] 토큰 응답 수신: {}", response);

        if (response == null || !response.containsKey("access_token")) {
            throw new RuntimeException("카카오 Access Token을 발급 실패");
        }

        String accessToken = (String) response.get("access_token");
        log.info("[KAKAO OAuth] AccessToken 발급 성공: {}", accessToken);
        return accessToken;
    }

    /**
     * 사용자 정보 요청 + DB 저장 및 JWT 발급
     */
    @Transactional
    public Map<String, Object> getUserInfo(String accessToken) {
        String userInfoUrl = "https://kapi.kakao.com/v2/user/me";

        log.info("[KAKAO] 사용자 정보 요청 시작");

        Map<String, Object> response = webClient.get()
                .uri(userInfoUrl)
                .headers(headers -> {
                    headers.setBearerAuth(accessToken);
                    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                })
                .retrieve()
                .bodyToMono(Map.class)
                .onErrorResume(e -> {
                    log.error("[KAKAO] 사용자 정보 요청 실패: {}", e.getMessage(), e);
                    return Mono.error(new RuntimeException("카카오 사용자 정보 요청 중 오류 발생"));
                })
                .block();

        log.info("[KAKAO] 사용자 정보 응답: {}", response);

        Map<String, Object> kakaoAccount = (Map<String, Object>) response.get("kakao_account");
        Map<String, Object> profile = kakaoAccount != null
                ? (Map<String, Object>) kakaoAccount.get("profile")
                : null;

        String email = kakaoAccount != null ? (String) kakaoAccount.get("email") : null;
        String nickname = profile != null ? (String) profile.get("nickname") : "카카오사용자";
        String profileImageUrl = profile != null ? (String) profile.get("profile_image_url") : null;
        Long kakaoId = ((Number) response.get("id")).longValue();

        // 카카오 이메일 없을 경우 대체 이메일 생성
        if (email == null || email.isEmpty()) {
            email = "kakao_" + kakaoId + "@noemail.com";
        }

        // 기존 회원 여부 확인
        Optional<UserEntity> existingUser = userRepository.findByEmail(email);

        UserEntity user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
            log.info("기존 카카오 사용자 로그인: {}", user.getUserName());
        } else {
            // 신규 회원 생성
            UserRoleEntity defaultRole = userRoleRepository.findById("USER")
                    .orElseThrow(() -> new RuntimeException("USER 권한이 존재하지 않습니다."));

            user = new UserEntity();
            user.setUserId("kakao_" + kakaoId);
            user.setUserName(nickname);
            user.setUserNick(nickname);
            user.setEmail(email);
            user.setPassword("SOCIAL_LOGIN");
            user.setBirth(LocalDate.of(1990, 1, 1));
            user.setAgency("kakao");
            user.setPhone("000-0000-0000");
            user.setAddr("카카오 로그인 사용자");
            user.setAddrDetail("");
            user.setSocialType("kakao");
            user.setStatus("active");
            user.setUserRole(defaultRole);

            userRepository.save(user);
            log.info("신규 카카오 사용자 등록: {}", user.getUserName());
        }

        // 프로필 이미지가 존재하면 DB에 저장
        if(profileImageUrl != null && !profileImageUrl.isEmpty()) {

            // 이미 프로필이 존재하는지 확인
            boolean hasProfileImage = fileRepository
                    .existsProfileFile(user.getUserId(), "profile") == 1;

            // 존재하는 프로필이 없을 경우
            if(!hasProfileImage) {
                try{
                    fileService.uploadFromUrl(user.getUserId(), profileImageUrl, "profile");
                    log.info("[KAKAO] 프로필 이미지 저장 완료: {}", profileImageUrl);
                } catch (IOException e) {
                    log.warn("[KAKAO] 프로필 이미지 저장 실패: {}", e.getMessage());
                }
            } else {
                log.info("[KAKAO] 기존 프로필 이미지가 존재하므로 새로 저장하지 않음");
            }
        }

        // JWT 발급
        String token = jwtUtils.createToken(
                "access",
                user.getUserId(),
                user.getUserName(),
                user.getUserRole().getRoleId(),
                1440
        );

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getUserId());
        result.put("userName", user.getUserName());
        result.put("userRole", user.getUserRole().getRoleId());
        result.put("socialType", user.getSocialType());

        log.info("[KAKAO] 로그인 완료 - userId={}, email={}", user.getUserId(), user.getEmail());
        return result;
    }
}
