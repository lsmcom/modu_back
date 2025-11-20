package back.code.oauth.controller;

import back.code.common.dto.ApiResponse;
import back.code.oauth.service.NaverOAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/oauth/naver")
public class NaverOAuthController {

    private final NaverOAuthService naverOAuthService;

    /**
     * ① 인가 코드로 Access Token 요청 및 사용자 정보 처리
     */
    @GetMapping("/callback")
    public ResponseEntity<ApiResponse<?>> naverCallback(@RequestParam("code") String code, @RequestParam("state") String state) {
        log.info("[NAVER CALLBACK] code={}, state={}", code, state);

        String accessToken = naverOAuthService.requestAccessToken(code, state);
        var userInfo = naverOAuthService.getUserInfo(accessToken);

        return ResponseEntity.ok(ApiResponse.ok(userInfo));
    }
}
