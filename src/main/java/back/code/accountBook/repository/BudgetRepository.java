package back.code.accountBook.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import back.code.accountBook.entity.BudgetEntity;
import back.code.user.entity.UserEntity;

public interface BudgetRepository extends JpaRepository<BudgetEntity, Integer>{

    // 모든 유저 ID 조회
    @Query(value = """
                    select distinct b.user.userId 
                    from BudgetEntity b
                """)
    List<String> findDistinctUserIds();

    // 전체 조회
    List<BudgetEntity> findAllByUser_UserIdAndYearMonth(String userId, String yearMonth);

    // 전체 예산 조회 (category_id IS NULL)
    Optional<BudgetEntity> findByUser_UserIdAndYearMonthAndCategoryIsNull(String userId, String yearMonth);

    // 카테고리별 예산 조회
    Optional<BudgetEntity> findByUser_UserIdAndCategory_CategoryIdAndYearMonth(
                                                                 String userId, Integer categoryId, String yearMonth);

    // 카테고리별 예산 중 첫 번째 조회 (임계값 확인용)
    Optional<BudgetEntity> findFirstByUser_UserIdAndYearMonthAndCategoryIsNotNull(String userId, String yearMonth);

    // 전체 예산 임계값 수정
    @Modifying
    @Query("""
        update BudgetEntity b 
        set b.threshold = :threshold 
        where b.user.userId = :userId 
            and b.yearMonth = :yearMonth 
            and b.category is null
    """)
    void updateTotalBudgetThreshold(@Param("userId") String userId,
                                    @Param("yearMonth") String yearMonth,
                                    @Param("threshold") Integer threshold);

    // 모든 카테고리별 예산 임계값 수정
    @Modifying
    @Query("""
        update BudgetEntity b 
        set b.threshold = :threshold 
        where b.user.userId = :userId 
            and b.yearMonth = :yearMonth 
            and b.category is not null
    """)
    void updateCategoryBudgetThreshold(@Param("userId") String userId,
                                       @Param("yearMonth") String yearMonth,
                                       @Param("threshold") Integer threshold);

    // 해당 유저의 가계부 내역 삭제
    void deleteByUser(UserEntity user);

}
