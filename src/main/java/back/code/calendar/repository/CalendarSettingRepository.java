package back.code.calendar.repository;

import back.code.calendar.entity.CalendarSettingEntity;
import back.code.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CalendarSettingRepository extends JpaRepository<CalendarSettingEntity, Long> {
    Optional<CalendarSettingEntity> findByUser(UserEntity user);

    // 해당 유저의 내역 삭제
    void deleteByUser(UserEntity user);
}
