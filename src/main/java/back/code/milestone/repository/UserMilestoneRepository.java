package back.code.milestone.repository;

import back.code.milestone.entity.UserMilestone;
import back.code.milestone.entity.UserMilestoneId;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface UserMilestoneRepository extends JpaRepository<UserMilestone, UserMilestoneId> {

    /**
     * 특정 사용자의 달성 기록과 Milestone 상세 정보를 조인하여 조회합니다.
     * JOIN FETCH를 사용하여 Milestone 객체를 한 번의 쿼리로 함께 로딩합니다 (N+1 문제 방지).
     * @param userId 사용자 ID
     * @return UserMilestone 목록 (Milestone 정보 포함, 달성 시간 최신순 정렬)
     */
    @Query("SELECT um FROM UserMilestone um JOIN FETCH um.milestone WHERE um.id.userId = :userId ORDER BY um.achievedAt DESC")
    List<UserMilestone> findByUserIdWithMilestone(@Param("userId") String userId);

    /*특정 사용자가 특정 업적을 이미 달성했는지 확인합니다.*/
    boolean existsById_UserIdAndId_MilestoneId(String userId, Long milestoneId);

    // 해당 유저의 내역 삭제
    @Modifying
    @Query("DELETE FROM UserMilestone um WHERE um.id.userId = :userId")
    void deleteByUserId(@Param("userId") String userId);
}