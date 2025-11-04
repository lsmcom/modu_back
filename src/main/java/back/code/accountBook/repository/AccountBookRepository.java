package back.code.accountBook.repository;

import back.code.accountBook.entity.AccountBookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AccountBookRepository extends JpaRepository<AccountBookEntity, Integer> {

    // 가계부 리스트(일별) 조회
    @Query("""
            select a
            from AccountBookEntity a
            where a.userId.userId = :userId
              and a.date between :monthStart and :monthEnd
            order by a.date desc
            """)
    List<AccountBookEntity> findDailyByUserIdAndDateBetween(@Param("userId") String userId,
                                                            @Param("monthStart") LocalDate monthStart,
                                                            @Param("monthEnd") LocalDate monthEnd);

    // 가계부 리스트(주별) 조회
    @Query(value = """
            select 
                date_format(date_add(a.date, interval (1 - DAYOFWEEK(a.date)) day), '%Y-%m-%d') as weekStart,
                date_format(date_add(a.date, interval(7 - DAYOFWEEK(a.date)) day), '%Y-%m-%d') as weekEnd,
                coalesce(sum(case when a.type = 'INCOME' then a.amount else 0 end), 0) as income,
                coalesce(sum(case when a.type = 'EXPENSE' then a.amount else 0 end), 0) as expense
            from account_book a
            where a.user_id = :userId
              and a.date between :startDate and :endDate
            group by weekStart, weekEnd
            order by weekStart asc 
            """, nativeQuery = true)
    List<Object[]> findByUserIdAndDateBetween(@Param("userId") String userId,
                                                       @Param("startDate") LocalDate startDate,
                                                       @Param("endDate") LocalDate endDate);

    // 가계부 리스트(달력) 조회
    @Query("""
            select 
                COALESCE(SUM(case when a.type = 'income' then a.amount else 0 end), 0),
                COALESCE(SUM(case when a.type = 'expense' then a.amount else 0 end), 0)
            from AccountBookEntity a
            where a.userId.userId = :userId
              and a.date = :date
            """)
    List<Object[]> findCalendarByUserIdAndDate(@Param("userId") String userId, @Param("date") LocalDate date);

}
