package back.code.admin.repository;

import back.code.inquiry.entity.InquiryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminInquiryRepository extends JpaRepository<InquiryEntity,Integer> {

    // isPublic = false 인 Inquiry 조회
    List<InquiryEntity> findByIsPublicFalse();

    // isPublic이 Y인 데이터만 조회
    List<InquiryEntity> findByIsPublic(boolean isPublic);

}
