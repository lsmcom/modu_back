package back.code.accountBook.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import back.code.accountBook.entity.AccountBookEntity;

public interface AccountSearchRepository extends JpaRepository<AccountBookEntity, Integer>, 
                                                 JpaSpecificationExecutor<AccountBookEntity>{


}
