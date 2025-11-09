package back.code.inquiry.controller;

import back.code.inquiry.dto.InquiryDetailResponseDto; // 4. 코드 내 수정된 부분을 명확히 표시: 파일 경로 수정
import back.code.inquiry.dto.InquiryListResponseDto; // 4. 코드 내 수정된 부분을 명확히 표시: 파일 경로 수정
import back.code.inquiry.dto.InquiryReplyRequestDto; // 4. 코드 내 수정된 부분을 명확히 표시: 파일 경로 수정
import back.code.inquiry.dto.InquiryReplyResponseDto; // 4. 코드 내 수정된 부분을 명확히 표시: 파일 경로 수정
import back.code.inquiry.dto.InquirySaveRequestDto; // 4. 코드 내 수정된 부분을 명확히 표시: 파일 경로 수정
import back.code.inquiry.service.InquiryReplyService; // 4. 코드 내 수정된 부분을 명확히 표시: 파일 경로 수정
import back.code.inquiry.service.InquiryService; // 4. 코드 내 수정된 부분을 명확히 표시: 파일 경로 수정
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 4. 코드 내 수정된 부분을 명확히 표시: Inquiry 관련 API 컨트롤러 (import 경로 수정)
@RestController
@RequestMapping("/api/inquiries")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;
    private final InquiryReplyService replyService;

    // 1. 목록 조회
    @GetMapping
    public ResponseEntity<List<InquiryListResponseDto>> getInquiryList(
            @RequestParam("userId") String userId,
            @PageableDefault(sort = "createAt", direction = Sort.Direction.DESC) Pageable pageable) {

        List<InquiryListResponseDto> list = inquiryService.getInquiryList(userId, pageable);
        return ResponseEntity.ok(list);
    }

    // 2. 상세 조회
    @GetMapping("/{inquiryId}")
    public ResponseEntity<InquiryDetailResponseDto> getInquiryDetail(@PathVariable Long inquiryId) {
        InquiryDetailResponseDto detail = inquiryService.getInquiryDetail(inquiryId);
        return ResponseEntity.ok(detail);
    }

    // 3. 등록
    @PostMapping
    public ResponseEntity<InquiryDetailResponseDto> createInquiry(@RequestBody InquirySaveRequestDto requestDto) {
        InquiryDetailResponseDto createdInquiry = inquiryService.createInquiry(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdInquiry);
    }

    // 4. 수정
    @PutMapping("/{inquiryId}")
    public ResponseEntity<InquiryDetailResponseDto> updateInquiry(
            @PathVariable Long inquiryId,
            @RequestBody InquirySaveRequestDto requestDto) {
        InquiryDetailResponseDto updatedInquiry = inquiryService.updateInquiry(inquiryId, requestDto);
        return ResponseEntity.ok(updatedInquiry);
    }

    // 5. 삭제
    @DeleteMapping("/{inquiryId}")
    public ResponseEntity<Void> deleteInquiry(@PathVariable Long inquiryId) {
        inquiryService.deleteInquiry(inquiryId);
        return ResponseEntity.noContent().build();
    }

    // 6. 검색
    @GetMapping("/search")
    public ResponseEntity<Page<InquiryListResponseDto>> searchInquiries(
            @RequestParam("userId") String userId,
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "range", defaultValue = "제목+내용") String range,
            Pageable pageable) {

        Page<InquiryListResponseDto> searchResult = inquiryService.searchInquiries(userId, keyword, range, pageable);
        return ResponseEntity.ok(searchResult);
    }

    // 7. 댓글 등록
    @PostMapping("/{inquiryId}/replies")
    public ResponseEntity<List<InquiryReplyResponseDto>> createReply(
            @PathVariable Long inquiryId,
            @RequestBody InquiryReplyRequestDto requestDto) {
        List<InquiryReplyResponseDto> replies = replyService.createReply(inquiryId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(replies);
    }
}