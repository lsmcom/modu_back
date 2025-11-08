package back.code.accountBook.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import back.code.accountBook.entity.AccountBookEntity;
import back.code.accountBook.entity.RecurringSettingEntity;

public interface RecurringSettingRepository extends JpaRepository<RecurringSettingEntity,Integer>{
    Optional<RecurringSettingEntity> findByAccount(AccountBookEntity account);

}
