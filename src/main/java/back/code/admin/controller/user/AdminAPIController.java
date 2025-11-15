package back.code.admin.controller.user;

import back.code.admin.dto.user.UserAllInfoDTO;
import back.code.admin.service.user.AdminUserService;
import back.code.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/user")
// 관리자 권한 필요
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminAPIController {

    private final AdminUserService  adminUserService;


    /** 전체 사용자 정보 조회 */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<UserAllInfoDTO>>> getAllUsers() {
        List<UserAllInfoDTO> users = adminUserService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.ok(users));
    }
}
