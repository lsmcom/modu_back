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
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserSettingEntity>> getUserSetting(@PathVariable String userId) {
        UserSettingEntity setting = userSettingService.getUserSetting(userId);
        return ResponseEntity.ok(ApiResponse.ok(setting));
    }

    /** 사용자 설정 수정 */
    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<String>> updateUserSetting(@PathVariable String userId,
            @Valid @RequestBody UserSettingUpdateDTO dto) {

        userSettingService.updateUserSetting(userId, dto);
        return ResponseEntity.ok(ApiResponse.ok("사용자 설정이 변경되었습니다."));
    }

    /** 데이터 초기화 */
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<String>> resetData(@PathVariable String userId) throws Exception {
        userSettingService.resetData(userId);
        
        return ResponseEntity.ok(ApiResponse.ok("데이터 초기화가 완료되었습니다."));
    }
}
