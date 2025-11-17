package back.code.admin.controller.announcement;


import back.code.admin.dto.announcement.AnnouncementCreateDTO;
import back.code.admin.dto.announcement.AnnouncementResponseDTO;
import back.code.admin.service.announcement.AdminAnnouncementService;
import back.code.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/announcement")
// 관리자 권한 필요
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminAnnouncementController {

    private final AdminAnnouncementService adminAnnouncementService;

    /**
     * Inquiry 공개 공지 + Community boardId=1 공지 통합 조회
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<AnnouncementResponseDTO>>> getAllAnnouncements() {

        List<AnnouncementResponseDTO> result = adminAnnouncementService.getAllAnnouncements();

        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // 공지 추가 기능
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<String>> createAnnouncement(
            @RequestBody AnnouncementCreateDTO dto) {

        adminAnnouncementService.createAnnouncement(dto);

        return ResponseEntity.ok(ApiResponse.ok("공지 등록 완료"));
    }

    // 공지 삭제(커뮤니티)
    @DeleteMapping("/community/{id}")
    public ResponseEntity<ApiResponse<String>> deleteCommunity(@PathVariable Integer id) {
        adminAnnouncementService.deleteCommunity(id);
        return ResponseEntity.ok(ApiResponse.ok("커뮤니티 공지 삭제 완료"));
    }

    // 공지 삭제(문의사항)
    @DeleteMapping("/inquiry/{id}")
    public ResponseEntity<ApiResponse<String>> deleteInquiry(@PathVariable Long id) {
        adminAnnouncementService.deleteInquiry(id);
        return ResponseEntity.ok(ApiResponse.ok("문의 공지 삭제 완료"));
    }

    // 공지 수정(커뮤니티)
    @PutMapping("/community/{id}")
    public ResponseEntity<ApiResponse<String>> updateCommunity(
            @PathVariable Integer id,
            @RequestBody AnnouncementCreateDTO dto) {

        adminAnnouncementService.updateCommunity(id, dto);
        return ResponseEntity.ok(ApiResponse.ok("커뮤니티 공지 수정 완료"));
    }

    // 공지 수정(문의사항)
    @PutMapping("/inquiry/{id}")
    public ResponseEntity<ApiResponse<String>> updateInquiry(
            @PathVariable Long id,
            @RequestBody AnnouncementCreateDTO dto) {

        adminAnnouncementService.updateInquiry(id, dto);
        return ResponseEntity.ok(ApiResponse.ok("문의 공지 수정 완료"));
    }
}
