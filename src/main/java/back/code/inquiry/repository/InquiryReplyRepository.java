package back.code.inquiry.repository;

import back.code.inquiry.entity.InquiryReply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// 4. 코드 내 수정된 부분을 명확히 표시: 정렬 기준 repliedAt -> createAt으로 변경
public interface InquiryReplyRepository extends JpaRepository<InquiryReply, Long> {

    // 4. 코드 내 수정된 부분을 명확히 표시: 특정 문의에 달린 답변 목록 조회 (정렬 기준 변경)
    List<InquiryReply> findByInquiry_InquiryIdOrderByCreateAtAsc(Long inquiryId); // 4. 코드 내 수정된 부분을 명확히 표시: RepliedAt -> CreateAt

    long countByInquiry_InquiryId(Long inquiryId);
}