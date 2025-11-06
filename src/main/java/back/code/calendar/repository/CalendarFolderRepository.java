package back.code.calendar.repository;

import back.code.calendar.entity.CalendarFolderEntity;
import back.code.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalendarFolderRepository extends JpaRepository<CalendarFolderEntity, String> {

    // 특정 사용자의 모든 폴더 조회
    List<CalendarFolderEntity> findByUser(UserEntity user);
}
