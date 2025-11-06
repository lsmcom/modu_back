package back.code.calendar.repository;

import back.code.calendar.entity.PlanEntity;
import back.code.calendar.entity.PlanShareEntity;
import back.code.calendar.entity.PlanSharedUserMapId;
import back.code.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanShareRepository extends JpaRepository<PlanShareEntity, PlanSharedUserMapId> {

    List<PlanShareEntity> findByPlan(PlanEntity plan);

    List<PlanShareEntity> findBySharedUser(UserEntity user);
}

