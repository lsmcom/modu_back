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
public class NaverOAuthService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final JWTUtils jwtUtils;
    private final FileService fileService;
    private final FileRepository fileRepository;

    private final WebClient webClient = WebClient.create();

    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.client-secret}")
    private String clientSecret;

    @Value("${naver.redirect-uri}")
    private String redirectUri;

    /**
     * 인가 코드 → Access Token 요청
     */
    public String requestAccessToken(String code, String state) {
        String tokenUrl = "https://nid.naver.com/oauth2.0/token";

        log.info("[NAVER] 토큰 요청 시작");

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

        log.info("[NAVER] 토큰 응답 수신: {}", response);

        if (response == null || !response.containsKey("access_token")) {
            throw new RuntimeException("네이버 Access Token 발급 실패");
        }

        String accessToken = (String) response.get("access_token");
        log.info("[NAVER OAuth] AccessToken 발급 성공: {}", accessToken);
        return accessToken;
    }

    /**
     * 사용자 정보 요청 + DB 저장 + JWT 발급
     */
    @Transactional
    public Map<String, Object> getUserInfo(String accessToken) {
        String userInfoUrl = "https://openapi.naver.com/v1/nid/me";

        Map<String, Object> response = webClient.get()
                .uri(userInfoUrl)
                .headers(headers -> {
                    headers.setBearerAuth(accessToken);
                    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                })
                .retrieve()
                .bodyToMono(Map.class)
                .onErrorResume(e -> {
                    log.error("[NAVER] 사용자 정보 요청 실패: {}", e.getMessage(), e);
                    return Mono.error(new RuntimeException("카카오 사용자 정보 요청 중 오류 발생"));
                })
                .block();

        log.info("[NAVER] 사용자 정보 응답: {}", response);

        Map<String, Object> naverAccount = (Map<String, Object>) response.get("response");
        String naverId = (String) naverAccount.get("id");
        String email = (String) naverAccount.get("email");
        String name = (String) naverAccount.get("name");
        String nickname = (String) naverAccount.get("nickname");
        String profileImageUrl = (String) naverAccount.get("profile_image");
        String birthyear = (String) naverAccount.get("birthyear");
        String birthday = (String) naverAccount.get("birthday");
        String mobile = (String) naverAccount.get("mobile");

        // 널체크 + 기본값 처리
        String finalName = name != null ? name : (nickname != null ? nickname : "네이버사용자");
        String finalEmail = email != null ? email : "naver_" + naverId + "@noemail.com";
        String finalPhone = mobile != null ? mobile : "000-0000-0000";
        LocalDate birth;
        if (birthyear != null && birthday != null && birthday.contains("-")) {
            try {
                String[] parts = birthday.split("-");
                int month = Integer.parseInt(parts[0]);
                int day = Integer.parseInt(parts[1]);
                birth = LocalDate.of(Integer.parseInt(birthyear), month, day);
            } catch (Exception e) {
                log.warn("[NAVER] 생년월일 파싱 실패: birthyear={}, birthday={}", birthyear, birthday);
                birth = LocalDate.of(1990, 1, 1);
            }
        } else {
            birth = LocalDate.of(1990, 1, 1);
        }

        // 기존 회원 조회
        Optional<UserEntity> existingUser = userRepository.findByEmail(email);

        UserEntity user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
            log.info("기존 네이버 사용자 로그인: {}", user.getUserName());
        } else {
            // 신규 회원 등록
            UserRoleEntity defaultRole = userRoleRepository.findById("USER")
                    .orElseThrow(() -> new RuntimeException("USER 권한이 존재하지 않습니다."));

            user = new UserEntity();
            user.setUserId("naver_" + naverId);
            user.setUserName(finalName);
            user.setUserNick(nickname != null ? nickname : finalName);
            user.setEmail(finalEmail);
            user.setPassword("SOCIAL_LOGIN");
            user.setBirth(birth);
            user.setAgency("naver");
            user.setPhone(finalPhone);
            user.setAddr("네이버 로그인 사용자");
            user.setAddrDetail("");
            user.setSocialType("naver");
            user.setStatus("active");
            user.setUserRole(defaultRole);

            userRepository.saveAndFlush(user);
            log.info("신규 네이버 사용자 등록: {}", user.getUserName());
        }

        // 프로필 이미지가 존재하면 DB에 저장
        if(profileImageUrl != null && !profileImageUrl.isEmpty()) {

            // 이미 프로필이 존재하는지 확인
            boolean hasProfileImage = fileRepository
                    .existsProfileFile(user.getUserId(), "profile") == 1;

            // 존재하는 프로필이 없을 경우
            if(!hasProfileImage) {
                try {
                    fileService.uploadFromUrl(user.getUserId(), profileImageUrl, "profile");
                    log.info("[NAVER] 프로필 이미지 저장 완료: {}", profileImageUrl);
                } catch (IOException e) {
                    log.warn("[NAVER] 프로필 이미지 저장 실패: {}", e.getMessage());
                }
            } else {
                log.info("[NAVER] 기존 프로필 이미지가 존재하므로 새로 저장하지 않음");
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

        // 프론트 반환 데이터 구성
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getUserId());
        result.put("userName", user.getUserName());
        result.put("userRole", user.getUserRole().getRoleId());
        result.put("socialType", user.getSocialType());

        log.info("[NAVER] 로그인 완료 - userId={}, email={}", user.getUserId(), user.getEmail());
        return result;
    }
}
