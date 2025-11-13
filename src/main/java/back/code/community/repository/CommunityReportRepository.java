package back.code.community.repository;

import back.code.community.entity.CommunityReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CommunityReportRepository extends JpaRepository<CommunityReportEntity, Integer> {
    Optional<CommunityReportEntity> findByPostIdAndUserId(Integer postId, String userId);
}
