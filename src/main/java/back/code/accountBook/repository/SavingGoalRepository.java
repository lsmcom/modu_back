package back.code.accountBook.repository;

import back.code.accountBook.entity.SavingsGoalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavingGoalRepository extends JpaRepository<SavingsGoalEntity, Integer> {

}
