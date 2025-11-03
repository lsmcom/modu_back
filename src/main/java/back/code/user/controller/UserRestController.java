package back.code.user.controller;

import back.code.common.dto.ApiResponse;
import back.code.user.dto.JoinRequestDTO;
import back.code.user.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserRestController {

    private final UserService userService;

    /** 아이디 중복 확인 */
    @GetMapping("/check/id")
    public ResponseEntity<ApiResponse<String>> checkUserId(@RequestParam String userId) {
        String result = userService.checkDuplicateUserId(userId);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /** 이메일 중복확인 + 인증코드 발송 */
    @GetMapping("/check/email")
    public ResponseEntity<ApiResponse<String>> checkEmail(@RequestParam String email) {
        String result = userService.checkDuplicateEmailAndSendCode(email);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /** 이메일 인증번호 검증 */
    @PostMapping("/verify/email")
    public ResponseEntity<ApiResponse<String>> verifyEmail(
            @RequestParam String email,
            @RequestParam String code) {

        String result = userService.verifyEmailCode(email, code);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /** 닉네임 중복 확인 */
    @GetMapping("/check/nick")
    public ResponseEntity<ApiResponse<String>> checkNick(@RequestParam String nick) {
        String result = userService.checkDuplicateNick(nick);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /** 전화번호 중복 확인 */
    @GetMapping("/check/phone")
    public ResponseEntity<ApiResponse<String>> checkPhone(@RequestParam String phone) {
        String result = userService.checkDuplicatePhone(phone);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // 회원가입
    @PostMapping("/join")
    public ResponseEntity<ApiResponse<String>> join(@Valid @RequestBody JoinRequestDTO dto) {
        userService.join(dto);
        return ResponseEntity.ok(ApiResponse.ok("회원가입이 완료되었습니다."));
    }

    /**
     * 아이디 찾기 - 이메일로 인증코드 발송
     */
    @GetMapping("/find/id/send")
    public ResponseEntity<ApiResponse<String>> sendFindIdEmail(@RequestParam String email) throws MessagingException {
        userService.sendFindIdEmail(email);
        return ResponseEntity.ok(ApiResponse.ok("인증번호가 이메일로 전송되었습니다."));
    }

    /**
     * 아이디 찾기 - 인증번호 검증 및 아이디 반환
     */
    @PostMapping("/find/id/verify")
    public ResponseEntity<ApiResponse<String>> verifyFindIdCode(@RequestParam String email, @RequestParam String code) {
        String userId = userService.verifyFindIdCode(email, code);
        return ResponseEntity.ok(ApiResponse.ok(userId));
    }

    /**
     * 비밀번호 찾기 - 인증코드 발송
     */
    @GetMapping("/find/pw/send")
    public ResponseEntity<ApiResponse<String>> sendFindPwEmail(@RequestParam String userId,
            @RequestParam String email) throws MessagingException {
        userService.sendFindPwEmail(userId, email);
        return ResponseEntity.ok(ApiResponse.ok("비밀번호 재설정을 위한 인증코드가 이메일로 전송되었습니다."));
    }

    /**
     * 비밀번호 찾기 - 인증번호 검증
     */
    @PostMapping("/find/pw/verify")
    public ResponseEntity<ApiResponse<String>> verifyFindPwCode(@RequestParam String userId,
            @RequestParam String email, @RequestParam String code) {
        userService.verifyFindPwCode(userId, email, code);
        return ResponseEntity.ok(ApiResponse.ok("이메일 인증이 완료되었습니다. 비밀번호를 재설정하세요."));
    }

    /**
     * 비밀번호 재설정
     */
    @PostMapping("/reset/pw")
    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestParam String userId,
            @RequestParam String newPassword) {
        userService.resetPassword(userId, newPassword);
        return ResponseEntity.ok(ApiResponse.ok("비밀번호가 성공적으로 변경되었습니다."));
    }
}
