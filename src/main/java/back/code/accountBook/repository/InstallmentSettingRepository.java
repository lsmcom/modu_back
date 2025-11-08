package back.code.accountBook.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import back.code.accountBook.entity.AccountBookEntity;
import back.code.accountBook.entity.InstallmentSettingEntity;

public interface InstallmentSettingRepository extends JpaRepository<InstallmentSettingEntity, Integer>{
    Optional<InstallmentSettingEntity> findByAccount(AccountBookEntity account);

}
