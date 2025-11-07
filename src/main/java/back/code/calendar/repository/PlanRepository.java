package back.code.calendar.repository;

import back.code.calendar.entity.CalendarFolderEntity;
import back.code.calendar.entity.PlanEntity;
import back.code.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanRepository extends JpaRepository<PlanEntity, String> {

    /* 특정 사용자 전체 일정 (folder까지 즉시 로딩) */
    @Query("SELECT p FROM PlanEntity p JOIN FETCH p.folder WHERE p.user = :user")
    List<PlanEntity> findByUserWithFolder(@Param("user") UserEntity user);

    // 특정 사용자 전체 일정
    List<PlanEntity> findByUser(UserEntity user);

    // 특정 폴더의 일정
    List<PlanEntity> findByFolder(CalendarFolderEntity folder);
}

