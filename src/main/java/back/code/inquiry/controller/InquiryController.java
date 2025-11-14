package back.code.inquiry.controller;

import back.code.common.dto.ApiResponse;
import back.code.inquiry.dto.InquiryCreateRequest;
import back.code.inquiry.dto.InquiryDto;
import back.code.inquiry.service.InquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/inquiries")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;

    /** 전체 문의사항 조회 API */
    @GetMapping
    public ResponseEntity<ApiResponse<List<InquiryDto>>> getAllInquiries() {

        List<InquiryDto> inquiries = inquiryService.getAllInquiries();
        return ResponseEntity.ok(ApiResponse.ok(inquiries));
    }

    /**
     * 문의사항 작성 (파일 첨부 포함)
     * - multipart/form-data
     *   - meta: InquiryCreateRequest JSON
     *   - files: 첨부파일 배열
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Long>> createInquiry(@RequestPart("meta") InquiryCreateRequest meta,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) throws IOException {

        Long inquiryId = inquiryService.createInquiry(meta, files);
        return ResponseEntity.ok(ApiResponse.ok(inquiryId));
    }
}
