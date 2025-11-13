package back.code.calendar.repository;

import back.code.calendar.entity.PlanEntity;
import back.code.calendar.entity.PlanShareEntity;
import back.code.calendar.entity.PlanSharedUserMapId;
import back.code.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanShareRepository extends JpaRepository<PlanShareEntity, PlanSharedUserMapId> {

    List<PlanShareEntity> findByPlan(PlanEntity plan);

    List<PlanShareEntity> findBySharedUser(UserEntity user);

    @Query("SELECT ps FROM PlanShareEntity ps " +
            "JOIN FETCH ps.plan p " +
            "JOIN FETCH p.folder f " +
            "WHERE ps.sharedUser = :user")
    List<PlanShareEntity> findBySharedUserWithPlan(@Param("user") UserEntity user);

    @Query("""
    SELECT ps.plan 
    FROM PlanShareEntity ps 
    JOIN ps.sharedUser su 
    WHERE su.userId = :userId
""")
    List<PlanEntity> findPlansSharedWithUser(@Param("userId") String userId);

}

