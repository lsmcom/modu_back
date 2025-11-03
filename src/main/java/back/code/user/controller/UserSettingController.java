package back.code.user.controller;

import back.code.common.dto.ApiResponse;
import back.code.user.dto.UserSettingUpdateDTO;
import back.code.user.entity.UserSettingEntity;
import back.code.user.service.UserSettingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user/setting")
@RequiredArgsConstructor
public class UserSettingController {

    private final UserSettingService userSettingService;

    /** 사용자 설정 조회 */
    @GetMapping
    public ResponseEntity<ApiResponse<UserSettingEntity>> getUserSetting(@RequestParam String userId) {
        UserSettingEntity setting = userSettingService.getUserSetting(userId);
        return ResponseEntity.ok(ApiResponse.ok(setting));
    }

    /** 사용자 설정 수정 */
    @PutMapping
    public ResponseEntity<ApiResponse<String>> updateUserSetting(@RequestParam String userId,
            @Valid @RequestBody UserSettingUpdateDTO dto) {

        userSettingService.updateUserSetting(userId, dto);
        return ResponseEntity.ok(ApiResponse.ok("사용자 설정이 변경되었습니다."));
    }
}
