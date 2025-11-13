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
public interface PlanRepository extends JpaRepository<PlanEntity, Long> {

    /* 특정 사용자 전체 일정 (folder까지 즉시 로딩) */
    @Query("SELECT p FROM PlanEntity p JOIN FETCH p.folder WHERE p.user = :user")
    List<PlanEntity> findByUserWithFolder(@Param("user") UserEntity user);

    // userId 기반으로 일정 조회 (folder, user 함께 로딩)
    @Query("SELECT p FROM PlanEntity p JOIN FETCH p.folder f JOIN FETCH p.user u WHERE u.userId = :userId")
    List<PlanEntity> findByUser_UserId(@Param("userId") String userId);

    // 특정 사용자 전체 일정
    List<PlanEntity> findByUser(UserEntity user);

    // 특정 폴더의 일정
    List<PlanEntity> findByFolder(CalendarFolderEntity folder);

    // folderId로 직접 조회 + folder 즉시 로딩
    @Query("SELECT p FROM PlanEntity p JOIN FETCH p.folder WHERE p.folder.folderId = :folderId")
    List<PlanEntity> findByFolder_FolderId(@Param("folderId") Long folderId);

}

