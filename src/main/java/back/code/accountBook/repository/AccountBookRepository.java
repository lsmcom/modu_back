package back.code.accountBook.repository;

import back.code.accountBook.entity.AccountBookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AccountBookRepository extends JpaRepository<AccountBookEntity, Integer> {

    // 가계부 리스트(일별) 조회
    List<AccountBookEntity> findByUserId_UserIdAndDate(String userId, LocalDate date);

    // 가계부 리스트(주별) 조회
    @Query("""
            select a
            from AccountBookEntity a
            where a.userId.userId = :userId
                and a.date between :startDate and :endDate
            order by a.date desc 
            """)
    List<AccountBookEntity> findWeeklyByUserIdAndDateBetween(@Param("userId") String userId,
                                                             @Param("startDate") LocalDate startDate,
                                                             @Param("endDate") LocalDate endDate);

    // 가계부 리스트(월별) 조회
    @Query("""
            select a
            from AccountBookEntity a
            where a.userId.userId = :userId
                and a.date between :monthStart and :monthEnd
            order by a.date desc 
            """)
    List<AccountBookEntity> findMonthlyByUserIdAndDateBetween(@Param("userId") String userId,
                                                             @Param("monthStart") LocalDate monthStart,
                                                             @Param("monthEnd") LocalDate monthEnd);

}
