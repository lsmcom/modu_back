package back.code.calendar.repository;

import back.code.calendar.entity.CalendarFolderEntity;
import back.code.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalendarFolderRepository extends JpaRepository<CalendarFolderEntity, Long> {

    // 특정 사용자의 모든 폴더 조회
    List<CalendarFolderEntity> findByUser(UserEntity user);

    List<CalendarFolderEntity> findByUser_UserId(String userId);

    List<CalendarFolderEntity> findByUserAndFolderType(UserEntity user, String folderType);

    // (추가) 내 폴더 + 공유 폴더 전체 조회
    @Query("SELECT f FROM CalendarFolderEntity f WHERE f.user.userId = :userId OR f.folderType = 'SHARED'")
    List<CalendarFolderEntity> findByUserOrShared(String userId);

    /** 특정 유저에게 지정된 타입의 폴더 존재 여부 */
    boolean existsByUserAndFolderType(UserEntity user, String folderType);

}
