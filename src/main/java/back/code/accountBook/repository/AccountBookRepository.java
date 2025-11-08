package back.code.accountBook.repository;

import back.code.accountBook.dto.AccountProjection;
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
            where a.user.userId = :userId
              and a.date between :monthStart and :monthEnd
            order by a.date desc
            """)
    List<AccountBookEntity> findDailyByUserIdAndDateBetween(@Param("userId") String userId,
                                                            @Param("monthStart") LocalDate monthStart,
                                                            @Param("monthEnd") LocalDate monthEnd);

    // 가계부 리스트(주별) 조회
    @Query(value = """
                select 
                    date_add(a.date, interval -((dayofweek(a.date) + 7 - :weekStartNumber) % 7) day) as week_start,
            		date_add(a.date, interval (6 - (dayofweek(a.date) + 7 - :weekStartNumber) % 7) day) as week_end,
                    coalesce(sum(case when a.type = 'INCOME' then a.amount else 0 end), 0) as income,
                    coalesce(sum(case when a.type = 'EXPENSE' then a.amount else 0 end), 0) as expense
                from account_book a
                where a.user_id = :userId
                  and a.date between :startDate and :endDate
                group by weekstart, weekend
                order by weekstart desc
            """, nativeQuery = true)
    List<AccountProjection> findWeeklyByUserIdAndDateBetween(@Param("userId") String userId,
                                                       @Param("startDate") LocalDate startDate,
                                                       @Param("endDate") LocalDate endDate,
                                                       @Param("weekStartNumber") int weekStartNumber);


    // 가계부 리스트(월별) 조회
    @Query(value = """
            select
                date_format(a.date, '%Y-%m') as yearMonth,
                coalesce(sum(case when a.type = 'INCOME' then a.amount else 0 end), 0) as income,
                coalesce(sum(case when a.type = 'EXPENSE' then a.amount else 0 end), 0) as expense
            from account_book a
            where a.user_id = :userId
            group by yearMonth
            order by yearMonth desc
            """, nativeQuery = true)
    List<AccountProjection> findMonthlyByUserId(@Param("userId") String userId);

    // 가계부 리스트(캘린더) 조회
    @Query(value = """
        select
            date_format(a.date, '%Y-%m-%d') as date,
            coalesce(sum(case when a.type = 'INCOME' then a.amount else 0 end), 0) as income,
            coalesce(sum(case when a.type = 'EXPENSE' then a.amount else 0 end), 0) as expense
        from account_book a
        where a.user_id = :userId
          and a.date between :startDate and :endDate
        group by a.date
        order by a.date
        """, nativeQuery = true)
    List<AccountProjection> findDailyByUserIdAndDateRange(@Param("userId") String userId,
                                                          @Param("startDate") LocalDate startDate,
                                                          @Param("endDate") LocalDate endDate);

}
