package back.code.oauth.controller;

import back.code.common.dto.ApiResponse;
import back.code.oauth.service.KakaoOAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/oauth/kakao")
public class KakaoOAuthController {

    private final KakaoOAuthService kakaoOAuthService;

    /**
     * 카카오 로그인 인가코드 처리
     * React에서 code를 전달받아 → 카카오 서버에서 access_token → 사용자 정보 조회
     */
    @GetMapping("/callback")
    public ResponseEntity<ApiResponse<?>> kakaoCallback(@RequestParam("code") String code) {
        log.info("[KAKAO] 인가 코드 수신: {}", code);

        // 인가 코드로 토큰 요청
        String accessToken = kakaoOAuthService.getAccessToken(code);

        // 액세스 토큰으로 사용자 정보 요청
        var userInfo = kakaoOAuthService.getUserInfo(accessToken);

        // 정상 응답 (ApiResponse 포맷)
        return ResponseEntity.ok(ApiResponse.ok(userInfo));
    }
}
