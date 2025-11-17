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
        AND (p.setting.isPublic = 'Y' OR (:userId IS NOT NULL AND p.user.userId = :userId))
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
        AND (p.setting.isPublic = 'Y' OR (:userId IS NOT NULL AND p.user.userId = :userId))
        AND b.boardId = :boardId
        ORDER BY p.createAt DESC
    """)
    List<CommunityPostDTO> findPostDTOsByBoardId(@Param("userId") String userId, @Param("boardId") Integer boardId);

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

    // 사용자별 게시글 수 조회
    int countByUser_UserIdAndIsTemporary(String userId, Character isTemporary);

    // 인기 게시글 조회 (조회수 / 추천수 / 댓글수 순 정렬 + 기간 필터)
    @Query("""
        SELECT new back.code.community.dto.CommunityPostDTO(p.postId, b.boardId, b.boardName, u.userId, u.userNick,
            f.filePath, f.storedName, p.title, p.contents, p.readCount, p.likeCount, p.isTemporary, p.createAt)
        FROM CommunityPostEntity p
        JOIN p.board b
        JOIN p.user u
        LEFT JOIN u.files f ON f.fileType = 'PROFILE'
        WHERE p.isTemporary = 'N'
        AND (p.setting.isPublic = 'Y' OR (:userId IS NOT NULL AND p.user.userId = :userId))
        AND (:period = 'all' OR p.createAt >= :cutoff)
        ORDER BY
            CASE WHEN :sortBy = 'view' THEN p.readCount END DESC,
            CASE WHEN :sortBy = 'like' THEN p.likeCount END DESC,
            CASE WHEN :sortBy = 'comment' THEN (
                SELECT COUNT(c)
                FROM CommunityPostCommentEntity c
                WHERE c.post.postId = p.postId
            ) END DESC
    """)
    List<CommunityPostDTO> findPopularPosts(@Param("userId") String userId, @Param("cutoff") LocalDateTime cutoff,
            @Param("sortBy") String sortBy, @Param("period") String period
    );

    /** 특정 사용자가 쓴 게시글(임시글 제외)을 최신순으로 조회 (게시판까지 fetch) */
    @Query("""
        SELECT p
        FROM CommunityPostEntity p
        JOIN FETCH p.board b
        WHERE p.user.userId = :userId
          AND p.isTemporary = 'N'
        ORDER BY p.createAt DESC
    """)
    List<CommunityPostEntity> findUserPostsWithBoard(@Param("userId") String userId);
}
