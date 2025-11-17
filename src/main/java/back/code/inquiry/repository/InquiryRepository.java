package back.code.inquiry.repository;

import back.code.inquiry.entity.InquiryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InquiryRepository extends JpaRepository<InquiryEntity, Long> {

    // 전체 문의사항 조회 (최신순)
    List<InquiryEntity> findAllByOrderByCreateAtDesc();

    // 사용자별 게시글 수 조회
    int countByUser_UserId(String userId);

    // 사용자별 문의 목록 조회 (최신순)
    List<InquiryEntity> findByUser_UserIdOrderByCreateAtDesc(String userId);
}
