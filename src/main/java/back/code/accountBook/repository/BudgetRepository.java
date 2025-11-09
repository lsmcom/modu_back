package back.code.accountBook.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import back.code.accountBook.entity.BudgetEntity;

public interface BudgetRepository extends JpaRepository<BudgetEntity, Integer>{

    // 전체 조회
    @Query(value = """
            select b 
            from BudgetEntity b
            where b.user.userId = :userId
            and b.yearMonth = :yearMonth
        """)
    List<BudgetEntity> findAllByUserAndYearMonth(@Param("userId") String userId, @Param("yearMonth") String yearMonth);

    // 카테고리 없는 전체 조회
    @Query(value = """
            select b 
            from BudgetEntity b 
            where b.user.userId = :userId 
                and b.category is null 
                and b.yearMonth = :yearMonth
        """)
    Optional<BudgetEntity> findTotalBudget(@Param("userId") String userId, @Param("yearMonth") String yearMonth);
    
    // 카테고리별 조회
    Optional<BudgetEntity> findByUser_UserIdAndCategory_CategoryIdAndYearMonth(String userId, Integer categoryId, String yearMonth);
}
