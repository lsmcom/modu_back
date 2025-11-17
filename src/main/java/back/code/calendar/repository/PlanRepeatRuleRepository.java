package back.code.calendar.repository;

import back.code.calendar.entity.PlanRepeatRuleEntity;
import back.code.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanRepeatRuleRepository extends JpaRepository<PlanRepeatRuleEntity, Long> {

    void deleteByPlan_PlanId(Long planId);

    boolean existsByPlan_PlanId(Long planId);

    void deleteByPlan_User(UserEntity user);
}
