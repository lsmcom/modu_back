package back.code.admin.repository;

import back.code.community.entity.CommunityReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminReportRepository extends JpaRepository<CommunityReportEntity, Integer> {

}
