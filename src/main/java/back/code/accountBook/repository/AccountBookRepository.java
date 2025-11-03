package back.code.accountBook.repository;

import back.code.accountBook.entity.AccountBookEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AccountBookRepository extends JpaRepository<AccountBookEntity, Integer> {

    // 가계부 리스트(일별) 조회
    List<AccountBookEntity> findByUserId_UserIdAndDate(String userId, LocalDate date);

}
