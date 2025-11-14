package back.code.admin.repository;

import back.code.community.entity.CommunityPostEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminCommunityPostRepository extends JpaRepository<CommunityPostEntity, Integer> {
    List<CommunityPostEntity> findByBoardBoardId(Integer boardId);
}
