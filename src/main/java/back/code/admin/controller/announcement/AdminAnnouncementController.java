package back.code.admin.controller.announcement;


import back.code.admin.dto.announcement.AnnouncementResponseDTO;
import back.code.admin.service.announcement.AdminAnnouncementService;
import back.code.common.dto.ApiResponse;
import back.code.inquiry.entity.InquiryEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
