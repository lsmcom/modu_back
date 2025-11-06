package back.code.accountBook.repository;

import back.code.accountBook.entity.AccountFileMappingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountFileMappingRepository extends JpaRepository<AccountFileMappingEntity, Integer> {
}
