package back.code.community.repository;

import back.code.community.entity.CommunityPostSettingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CommunityPostSettingRepository extends JpaRepository<CommunityPostSettingEntity, Integer> {

    Optional<CommunityPostSettingEntity> findByPost_PostId(Integer postId);

    // 게시글 삭제 시 글쓰기 설정도 삭제
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from CommunityPostSettingEntity s where s.post.postId = :postId")
    int deleteByPost_PostId(@Param("postId") Integer postId);
}
