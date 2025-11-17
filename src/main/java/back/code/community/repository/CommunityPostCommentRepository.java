package back.code.community.repository;

import back.code.community.entity.CommunityPostCommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommunityPostCommentRepository extends JpaRepository<CommunityPostCommentEntity, Integer> {

    // 게시글 기준 댓글 전체 조회 (최상위 댓글 + 대댓글 포함)
    @Query("""
        SELECT c FROM CommunityPostCommentEntity c
        LEFT JOIN FETCH c.user u
        LEFT JOIN FETCH c.replies r
        LEFT JOIN FETCH r.user ru
        WHERE c.post.postId = :postId
        ORDER BY c.createAt ASC
    """)
    List<CommunityPostCommentEntity> findAllByPostIdWithReplies(@Param("postId") Integer postId);

    // 특정 부모 댓글에 대한 대댓글 조회
    List<CommunityPostCommentEntity> findByParentComment_CommentIdOrderByCreateAtAsc(Integer parentCommentId);

    // 특정 게시글의 모든 댓글 조회 (부모/자식 관계 무시)
    List<CommunityPostCommentEntity> findByPost_PostIdOrderByCreateAtAsc(Integer postId);

    // 게시글 별 댓글수 조회
    int countByPost_PostId(Integer postId);

    // 사용자 별 댓글수 조회
    int countByUser_UserId(String userId);

    /** 특정 사용자가 쓴 댓글 목록 (게시글까지 fetch) */
    @Query("""
        SELECT c
        FROM CommunityPostCommentEntity c
        JOIN FETCH c.post p
        WHERE c.user.userId = :userId
        ORDER BY c.createAt DESC
    """)
    List<CommunityPostCommentEntity> findUserCommentsWithPost(@Param("userId") String userId);
}
