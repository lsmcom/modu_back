package back.code.inquiry.repository;

import back.code.inquiry.entity.InquiryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InquiryRepository extends JpaRepository<InquiryEntity, Long> {

    // 전체 문의사항 조회 (최신순)
    List<InquiryEntity> findAllByOrderByCreateAtDesc();
}
