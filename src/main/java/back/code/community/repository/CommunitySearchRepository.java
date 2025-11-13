package back.code.community.repository;

import back.code.community.entity.CommunityPostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommunitySearchRepository extends JpaRepository<CommunityPostEntity, Integer> {

    /* 제목 + 작성자 검색 (게시판 선택 O) */
    @Query("""
        SELECT p
        FROM CommunityPostEntity p
        JOIN p.board b
        WHERE (:boardId IS NULL OR b.boardId = :boardId)
        AND (p.setting.isPublic = 'Y' OR (:userId IS NOT NULL AND p.user.userId = :userId))
        AND (p.setting.isSearch = 'Y' OR p.setting IS NULL)
        AND (
            LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(p.contents) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(p.user.userNick) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
        ORDER BY p.createAt DESC
    """)
    List<CommunityPostEntity> searchAll(@Param("keyword") String keyword, @Param("boardId") Integer boardId);

    /* 제목만 검색 */
    @Query("""
        SELECT p
        FROM CommunityPostEntity p
        JOIN p.board b
        WHERE (:boardId IS NULL OR b.boardId = :boardId)
        AND (p.setting.isPublic = 'Y' OR (:userId IS NOT NULL AND p.user.userId = :userId))
        AND (p.setting.isSearch = 'Y' OR p.setting IS NULL)
        AND LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
        ORDER BY p.createAt DESC
    """)
    List<CommunityPostEntity> searchByTitle(@Param("keyword") String keyword, @Param("boardId") Integer boardId);

    /* 작성자명만 검색 */
    @Query("""
        SELECT p
        FROM CommunityPostEntity p
        JOIN p.board b
        WHERE (:boardId IS NULL OR b.boardId = :boardId)
        AND (p.setting.isPublic = 'Y' OR (:userId IS NOT NULL AND p.user.userId = :userId))
        AND (p.setting.isSearch = 'Y' OR p.setting IS NULL)
        AND LOWER(p.user.userNick) LIKE LOWER(CONCAT('%', :keyword, '%'))
        ORDER BY p.createAt DESC
    """)
    List<CommunityPostEntity> searchByUserNick(@Param("keyword") String keyword, @Param("boardId") Integer boardId);
}
