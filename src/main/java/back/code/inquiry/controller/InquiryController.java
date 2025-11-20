package back.code.inquiry.controller;

import back.code.common.dto.ApiResponse;
import back.code.inquiry.dto.*;
import back.code.inquiry.service.InquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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

    /** 문의사항 상세 조회 */
    @GetMapping("/{inquiryId}")
    public ResponseEntity<?> getInquiryDetail(@PathVariable("inquiryId") Long inquiryId) {
        InquiryDetailResponse dto = inquiryService.getInquiryDetail(inquiryId);
        return ResponseEntity.ok(dto);
    }

    /** 파일 삭제 */
    @DeleteMapping("/{inquiryId}/files/{fileId}")
    public ResponseEntity<Void> deleteFile(@PathVariable("inquiryId") Long inquiryId, @PathVariable("fileId") String fileId) {
        inquiryService.deleteInquiryFile(inquiryId, fileId);
        return ResponseEntity.ok().build();
    }

    /** 문의사항 게시글 수정 */
    @PutMapping("/{inquiryId}")
    public ResponseEntity<?> updateInquiry(@PathVariable("inquiryId") Long inquiryId, @RequestPart("meta") InquiryUpdateRequest dto,
            @RequestPart(value = "files", required = false) List<MultipartFile> newFiles) throws IOException {
        dto.setInquiryId(inquiryId);
        Long updatedId = inquiryService.updateInquiry(dto, newFiles);
        return ResponseEntity.ok(Map.of("response", updatedId));
    }

    /** 문의사항 삭제 */
    @DeleteMapping("/{inquiryId}")
    public ResponseEntity<Void> deleteInquiry(@PathVariable("inquiryId") Long inquiryId) {
        inquiryService.deleteInquiry(inquiryId);
        return ResponseEntity.ok().build();
    }

    /** 사용자 별 문의사항 게시글 수 조회 */
    @GetMapping("/users/{userId}/inquiry-count")
    public ResponseEntity<ApiResponse<Integer>> getUserInquiryCount(@PathVariable("userId") String userId) {
        int result = inquiryService.getUserInquiryCount(userId);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /** 사용자별 문의사항 목록 조회 */
    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<List<MyInquiryListDTO>>> getUserInquiries(@PathVariable("userId") String userId) {
        List<MyInquiryListDTO> result = inquiryService.getUserInquiries(userId);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
