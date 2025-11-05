package back.code.accountBook.repository;

import back.code.accountBook.entity.AccountSavingsGoalEntity;
import back.code.user.entity.UserEntity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SavingGoalRepository extends JpaRepository<AccountSavingsGoalEntity, Integer> {

    // 특정 사용자의 모든 저축 목표
    List<AccountSavingsGoalEntity> findAllByUser(UserEntity user);
}
