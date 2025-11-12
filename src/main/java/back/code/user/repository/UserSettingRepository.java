package back.code.user.repository;

import back.code.user.entity.UserEntity;
import back.code.user.entity.UserSettingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSettingRepository extends JpaRepository<UserSettingEntity, String> {

    Optional<UserSettingEntity> findByUser(UserEntity user);
    
}
