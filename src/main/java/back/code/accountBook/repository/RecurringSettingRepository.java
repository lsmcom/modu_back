package back.code.accountBook.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import back.code.accountBook.entity.AccountBookEntity;
import back.code.accountBook.entity.RecurringSettingEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecurringSettingRepository extends JpaRepository<RecurringSettingEntity,Integer>{
    Optional<RecurringSettingEntity> findByAccount(AccountBookEntity account);

    // 스케줄러용
    @Query(value = """
            select r 
            from RecurringSettingEntity r
            where r.nextDate = :date 
                and r.isActive = true
            """)
    List<RecurringSettingEntity> findByNextDateAndIsActiveTrue(@Param("date") LocalDate date);

}
