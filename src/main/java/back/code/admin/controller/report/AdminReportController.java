package back.code.admin.controller.report;


import back.code.admin.dto.report.AdminReportDTO;
import back.code.admin.service.report.AdminReportService;
import back.code.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
