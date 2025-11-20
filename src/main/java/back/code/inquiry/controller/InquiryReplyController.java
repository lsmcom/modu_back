package back.code.inquiry.controller;

import back.code.inquiry.dto.InquiryReplyRequest;
import back.code.inquiry.dto.InquiryReplyResponse;
import back.code.inquiry.entity.InquiryReplyEntity;
import back.code.inquiry.service.InquiryReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/inquiries")
public class InquiryReplyController {

    private final InquiryReplyService replyService;

    /** 답변 등록 */
    @PostMapping("/{inquiryId}/replies")
    public ResponseEntity<?> createReply(@PathVariable("inquiryId") Long inquiryId, @RequestBody InquiryReplyRequest req) {
        req.setInquiryId(inquiryId);
        InquiryReplyResponse created = replyService.createReply(req);
        return ResponseEntity.ok(created);
    }

    /** 답변 조회 */
    @GetMapping("/{inquiryId}/replies")
    public ResponseEntity<?> getReplies(@PathVariable("inquiryId") Long inquiryId) {
        return ResponseEntity.ok(replyService.getReplies(inquiryId));
    }
}
