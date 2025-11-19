package back.code.admin.controller.report;


import back.code.admin.dto.report.AdminReportDTO;
import back.code.admin.dto.report.AdminReportStatusUpdateDTO;
import back.code.admin.service.report.AdminReportService;
import back.code.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/report")
// 관리자 권한 필요
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminReportController {

    private final AdminReportService adminReportService;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<AdminReportDTO>>> getReports() {
        return ResponseEntity.ok(ApiResponse.ok(adminReportService.getAllReports()));
    }

    /* 신고 상태 변경 */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<String>> updateReportStatus(
            @PathVariable("id") Integer reportId,
            @RequestBody AdminReportStatusUpdateDTO dto
    ) {
        adminReportService.updateReportStatus(reportId, dto.getStatus());
        return ResponseEntity.ok(ApiResponse.ok("상태 변경 완료"));
    }
}
