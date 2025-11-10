package back.code.inquiry.repository;

import back.code.inquiry.entity.Inquiry; // 4. 코드 내 수정된 부분을 명확히 표시: 파일 경로 수정
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

// 4. 코드 내 수정된 부분을 명확히 표시: Inquiry 엔티티 레포지토리
public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    Page<Inquiry> findByIsPublicFalseAndUser_UserId(String userId, Pageable pageable);

    List<Inquiry> findByIsPublicTrueOrderByCreateAtDesc();

    // 4. 코드 내 수정된 부분을 명확히 표시: 검색 기능 (제목+내용+작성자)
    @Query("SELECT i FROM Inquiry i WHERE i.isPublic = FALSE AND i.user.userId = :userId " +
            "AND (LOWER(i.title) LIKE %:keyword% OR LOWER(i.content) LIKE %:keyword% OR LOWER(i.user.userId) LIKE %:keyword%)")
    Page<Inquiry> searchPrivateInquiriesByAll(@Param("userId") String userId, @Param("keyword") String keyword, Pageable pageable);

    // 4. 코드 내 수정된 부분을 명확히 표시: 검색 기능 (제목+내용)
    @Query("SELECT i FROM Inquiry i WHERE i.isPublic = FALSE AND i.user.userId = :userId " +
            "AND (LOWER(i.title) LIKE %:keyword% OR LOWER(i.content) LIKE %:keyword%)")
    Page<Inquiry> searchPrivateInquiriesByTitleAndContent(@Param("userId") String userId, @Param("keyword") String keyword, Pageable pageable);

    // 4. 코드 내 수정된 부분을 명확히 표시: 검색 기능 (제목)
    Page<Inquiry> findByIsPublicFalseAndUser_UserIdAndTitleContainingIgnoreCase(String userId, String title, Pageable pageable);

    // 4. 코드 내 수정된 부분을 명확히 표시: 검색 기능 (작성자)
    Page<Inquiry> findByIsPublicFalseAndUser_UserIdAndUser_UserIdContainingIgnoreCase(String userId, String writerUserId, Pageable pageable);
}