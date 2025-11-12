package back.code.community.repository;

import back.code.community.dto.CommunityPostDTO;
import back.code.community.entity.CommunityPostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CommunityPostRepository extends JpaRepository<CommunityPostEntity, Integer> {

    // 게시글 전체 조회 (최신순)
    @Query("""
        SELECT new back.code.community.dto.CommunityPostDTO(
            p.postId, b.boardId, b.boardName, u.userId, u.userNick, f.filePath,
            f.storedName, p.title, p.contents, p.readCount, p.likeCount, p.isTemporary, p.createAt,
            CASE WHEN EXISTS (
                SELECT 1 FROM CommunityPostLikeEntity l
                WHERE l.post.postId = p.postId AND l.user.userId = :userId
            ) THEN true ELSE false END
        )
        FROM CommunityPostEntity p
        JOIN p.board b
        JOIN p.user u
        LEFT JOIN u.files f ON f.fileType = 'PROFILE'
        WHERE p.isTemporary = 'N'
        ORDER BY p.createAt DESC
    """)
    List<CommunityPostDTO> findAllPostSummariesWithLikeStatus(@Param("userId") String userId);

    // 게시판별 게시글 조회
    @Query("""
        SELECT new back.code.community.dto.CommunityPostDTO(
            p.postId, b.boardId, b.boardName, u.userId, u.userNick, f.filePath,
            f.storedName, p.title, p.contents, p.readCount, p.likeCount, p.isTemporary, p.createAt
        )
        FROM CommunityPostEntity p
        JOIN p.board b
        JOIN p.user u
        LEFT JOIN u.files f ON f.fileType = 'PROFILE'
        WHERE p.isTemporary = 'N'
        AND b.boardId = :boardId
        ORDER BY p.createAt DESC
    """)
    List<CommunityPostDTO> findPostDTOsByBoardId(@Param("boardId") Integer boardId);

    // 임시저장 게시글 조회
    @Query("""
        SELECT new back.code.community.dto.CommunityPostDTO(
            p.postId, 
            b.boardId, 
            b.boardName, 
            u.userId, 
            u.userNick, 
            f.filePath,
            f.storedName, 
            p.title, 
            p.contents, 
            p.readCount, 
            p.likeCount, 
            p.isTemporary, 
            p.createAt
        )
        FROM CommunityPostEntity p
        LEFT JOIN p.board b
        JOIN p.user u
        LEFT JOIN u.files f ON f.fileType = 'PROFILE'
        WHERE p.isTemporary = 'Y' 
        AND u.userId = :userId
        ORDER BY p.createAt DESC
    """)
    List<CommunityPostDTO> findTempPostsByUserId(@Param("userId") String userId);

    // 60일 지난 임시글 자동 삭제
    @Query("""
        SELECT p.postId FROM CommunityPostEntity p
        WHERE p.isTemporary = 'Y' AND p.createAt < :cutoff
    """)
    List<Integer> findOldTemporaryPostIds(@Param("cutoff") LocalDateTime cutoff);

    // 게시글의 글쓰기 설정 찾기
    @Query("""
        SELECT p FROM CommunityPostEntity p
        LEFT JOIN FETCH p.setting s
        WHERE p.postId = :postId
    """)
    Optional<CommunityPostEntity> findPostWithSetting(@Param("postId") Integer postId);
}
