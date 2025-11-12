package back.code.community.repository;

import back.code.community.entity.CommunityPostViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityPostViewRepository extends JpaRepository<CommunityPostViewEntity, Integer> {

    // 특정 사용자가 특정 게시글을 이미 조회했는지 확인
    boolean existsByPost_PostIdAndUser_UserId(Integer postId, String userId);
}
