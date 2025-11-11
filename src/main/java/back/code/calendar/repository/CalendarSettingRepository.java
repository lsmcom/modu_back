package back.code.calendar.repository;

import back.code.calendar.entity.CalendarSettingEntity;
import back.code.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CalendarSettingRepository extends JpaRepository<CalendarSettingEntity, Long> {
    Optional<CalendarSettingEntity> findByUser(UserEntity user);
}
