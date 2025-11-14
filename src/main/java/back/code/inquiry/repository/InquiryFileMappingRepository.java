package back.code.inquiry.repository;

import back.code.inquiry.entity.InquiryFileMappingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 문의사항 - 파일 매핑 레포지토리
 * - 한 문의글에 여러 파일이 연결될 수 있음
 */
public interface InquiryFileMappingRepository extends JpaRepository<InquiryFileMappingEntity, Long> {

    /** 특정 문의글에 연결된 모든 파일 매핑 조회 */
    List<InquiryFileMappingEntity> findByInquiry_InquiryId(Long inquiryId);

    /** 특정 문의글에 연결된 매핑 전체 삭제 */
    void deleteByInquiry_InquiryId(Long inquiryId);

    /** 특정 파일이 문의 매핑에 몇 번 쓰이는지 카운트 (고아 파일 삭제 시 사용) */
    long countByFile_FileId(String fileId);

    /** 문의 + 파일 한 건 조회 (파일까지 fetch join) */
    @Query("""
        select m 
        from InquiryFileMappingEntity m 
        join fetch m.file f 
        where m.inquiry.inquiryId = :inquiryId 
          and f.fileId = :fileId
    """)
    Optional<InquiryFileMappingEntity> findByInquiryIdAndFileId(
            @Param("inquiryId") Long inquiryId, @Param("fileId") String fileId
    );

    /** 문의글에 연결된 파일 + 파일 엔티티 전체 조회 (상세/삭제용) */
    @Query("""
        select m 
        from InquiryFileMappingEntity m 
        join fetch m.file f 
        where m.inquiry.inquiryId = :inquiryId
    """)
    List<InquiryFileMappingEntity> findByInquiryIdWithFile(@Param("inquiryId") Long inquiryId);
}
