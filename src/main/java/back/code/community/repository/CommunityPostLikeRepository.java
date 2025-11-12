package back.code.community.repository;

import back.code.community.entity.CommunityPostLikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CommunityPostLikeRepository extends JpaRepository<CommunityPostLikeEntity, Integer> {

    /** 특정 유저가 특정 게시글에 좋아요 눌렀는지 여부 확인 */
    boolean existsByPost_PostIdAndUser_UserId(Integer postId, String userId);

    /** 특정 유저의 좋아요 삭제 */
    void deleteByPost_PostIdAndUser_UserId(Integer postId, String userId);

    /** 게시글의 전체 좋아요 수 조회 */
    long countByPost_PostId(Integer postId);

    /** 특정 유저의 좋아요 엔티티 조회 (토글용) */
    Optional<CommunityPostLikeEntity> findByPost_PostIdAndUser_UserId(Integer postId, String userId);
}
