package back.code.user.controller;

import back.code.common.dto.ApiResponse;
import back.code.user.dto.JoinRequestDTO;
import back.code.admin.dto.user.UserAllInfoDTO;
import back.code.user.dto.UserInfoDTO;
import back.code.user.dto.UserUpdateRequest;
import back.code.user.repository.UserRepository;
import back.code.user.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserRestController {

    private final UserService userService;
    private final UserRepository userRepository;

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

    /**
     * 유저 프로필 이미지 조회 API
     */
    @GetMapping("/{userId}/profile-image")
    public ResponseEntity<ApiResponse<String>> getProfileImage(@PathVariable String userId) {
        String profileImagePath = userService.getProfileImage(userId);
        return ResponseEntity.ok(ApiResponse.ok(profileImagePath));
    }

    /**
     * 프로필 이미지 업로드 및 변경
     */
    @PostMapping("/{userId}/profile-image")
    public ResponseEntity<ApiResponse<String>> updateProfileImage(@PathVariable String userId,
            @RequestPart("file") MultipartFile file) throws IOException {
        userService.updateProfileImage(userId, file);
        return ResponseEntity.ok(ApiResponse.ok("프로필 이미지가 변경되었습니다."));
    }

    /**
     * 사용자 닉네임 조회
     */
    @GetMapping("/{userId}/nickname")
    public ResponseEntity<ApiResponse<String>> getUserNickname(@PathVariable String userId) {
        String nickname = userService.getUserNickname(userId);
        return ResponseEntity.ok(ApiResponse.ok(nickname));
    }

    /**
     * 닉네임 변경
     */
    @PutMapping("/{userId}/nickname")
    public ResponseEntity<ApiResponse<String>> updateNickname(@PathVariable String userId,
            @RequestBody Map<String, String> body) {
        String newNick = body.get("nickname");
        userService.updateNickname(userId, newNick);
        return ResponseEntity.ok(ApiResponse.ok("닉네임이 변경되었습니다."));
    }

    /** 회원정보 조회 */
    @GetMapping("/{userId}/info")
    public ResponseEntity<ApiResponse<UserInfoDTO>> getUserInfo(@PathVariable String userId) {
        UserInfoDTO result = userService.getUserInfo(userId);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /** 회원정보 수정 */
    @PutMapping("/{userId}/info")
    public ResponseEntity<ApiResponse<String>> updateUserInfo(@PathVariable String userId,
            @RequestBody UserUpdateRequest req) {
        userService.updateUserInfo(userId, req);
        return ResponseEntity.ok(ApiResponse.ok("회원정보가 수정되었습니다."));
    }

    /**
     * 비밀번호 변경
     */
    @PostMapping("/edit/pw")
    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestParam String userId,
            @RequestParam String currentPassword, @RequestParam String newPassword) {
        userService.editPassword(userId, currentPassword, newPassword);
        return ResponseEntity.ok(ApiResponse.ok("비밀번호가 성공적으로 변경되었습니다."));
    }

    /** 회원 탈퇴 */
    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<String>> withdrawUser(@RequestParam String userId,
            @RequestParam String password, @RequestParam(required = false) String reason) {
        userService.withdrawUser(userId, password, reason);
        return ResponseEntity.ok(ApiResponse.ok("회원 탈퇴가 완료되었습니다."));
    }

    // 아이디로 회원 존재 여부 검색
    @GetMapping("/exists/{userId}")
    public ResponseEntity<ApiResponse<Boolean>> checkUserExists(@PathVariable String userId) {
        boolean exists = userRepository.existsById(userId);
        return ResponseEntity.ok(ApiResponse.ok(exists));
    }

}
