package back.code.community.repository;

import back.code.community.entity.CommunityPostSettingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityPostSettingRepository extends JpaRepository<CommunityPostSettingEntity, Integer> {

    void deleteByPost_PostId(Integer postId);
}
