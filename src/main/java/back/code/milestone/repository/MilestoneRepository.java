package back.code.milestone.repository;

import back.code.milestone.entity.Milestone;
import back.code.milestone.entity.UserMilestone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface MilestoneRepository extends JpaRepository<Milestone, Long> {

    /**
     * 특정 사용자에게 아직 달성되지 않은 (user_milestone에 기록되지 않은) 업적 중,
     * 주어진 목표값(maxTargetValue) 이하인 업적 목록을 조회합니다.
     * * @param userId 업적 달성 여부를 확인할 사용자 ID
     * @param maxTargetValue 현재 사용자의 총 완료 개수
     * @param targetType 목표 유형 (예: "TODO_COMPLETE")
     * @return 달성 가능한 미달성 Milestone 목록
     */
    @Query(value = "SELECT m FROM Milestone m " +
            "WHERE m.targetType = :targetType " +
            "  AND m.targetValue <= :maxTargetValue " +
            "  AND m.milestoneId NOT IN (" +
            "      SELECT um.id.milestoneId FROM UserMilestone um WHERE um.id.userId = :userId" +
            "  )")
    List<Milestone> findUnachievedMilestones(
            @Param("userId") String userId,
            @Param("maxTargetValue") int maxTargetValue,
            @Param("targetType") String targetType
    );
}