package back.code.inquiry.repository;

import back.code.inquiry.entity.InquiryReplyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InquiryReplyRepository extends JpaRepository<InquiryReplyEntity, Long> {

    // 문의사항의 답변 조회 (최신순)
    List<InquiryReplyEntity> findByInquiry_InquiryIdOrderByCreateAtAsc(Long inquiryId);

    // 문의사항 삭제 시 댓글 삭제
    void deleteByInquiry_InquiryId(Long inquiryId);
}
