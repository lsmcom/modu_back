package back.code.community.repository;

import back.code.community.entity.CommunityBoardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommunityBoardRepository extends JpaRepository<CommunityBoardEntity, Integer> {
}
