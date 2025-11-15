package back.code.admin.controller.inquiry;


import back.code.admin.dto.inquiry.AdminInquiryDTO;
import back.code.admin.service.inquiry.AdminInquiryService;
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
@RequestMapping("/api/v1/admin/inquiry")
// 관리자 권한 필요
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminInquiryController {

    private final AdminInquiryService inquiryService;

    /**
     * isPublic = false (비공개 문의 조회)
     */
    @GetMapping("/private")
    public ResponseEntity<ApiResponse<List<AdminInquiryDTO>>> getPrivateInquiries() {

        List<AdminInquiryDTO> privateInquiries = inquiryService.getPrivateInquiries();

        return ResponseEntity.ok(ApiResponse.ok(privateInquiries));
    }

}
