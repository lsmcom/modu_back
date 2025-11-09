package back.code.accountBook.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import back.code.accountBook.dto.AccountSavingGoalDTO;
import back.code.accountBook.dto.BudgetDTO;
import back.code.accountBook.entity.AccountCategoryEntity;
import back.code.accountBook.entity.AccountSavingsGoalEntity;
import back.code.accountBook.entity.BudgetEntity;
import back.code.accountBook.repository.BudgetRepository;
import back.code.accountBook.repository.CategoryRepository;
import back.code.accountBook.repository.SavingGoalRepository;
import back.code.user.entity.UserEntity;
import back.code.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountSettingService {

    private final UserRepository userRepository;
    private final SavingGoalRepository savingGoalRepository;
    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;

    // 작성창용 저축목표
    @Transactional
    public List<AccountSavingGoalDTO.writeGoals> getWriteGoals(String userId) throws Exception{

        // 사용자 확인
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        // 사용자의 저축 목표 조회
        List<AccountSavingsGoalEntity> goals = savingGoalRepository.findAllByUser(user);
        // DTO 변환
        List<AccountSavingGoalDTO.writeGoals> result = new ArrayList<>();
        for (AccountSavingsGoalEntity goal : goals) {
            result.add(AccountSavingGoalDTO.writeGoals.of(goal));
        }

        return result;            
    }

    // 월별 예산 조회
    @Transactional
    public BudgetDTO.BudgetSettingResponse getBudgetSetting(String userId, String yearMonth) throws Exception{

        List<BudgetEntity> budgets = budgetRepository.findAllByUserAndYearMonth(userId, yearMonth);

        // 전체 예산
        Integer totalBudget = budgets.stream()
                            .filter(b -> b.getCategory() == null)
                            .findFirst()
                            .map(BudgetEntity::getBudgetAmount)
                            .orElse(null);
        
        // 카테고리별 예산
        List<BudgetDTO.CategoryBudgetInfo> categoryBudgets = budgets.stream()
                                .filter(b -> b.getCategory() != null)
                                .map(b -> BudgetDTO.CategoryBudgetInfo.builder()
                                        .categoryId(b.getCategory().getCategoryId())
                                        .categoryName(b.getCategory().getCategoryName())
                                        .budgetAmount(b.getBudgetAmount())
                                        .build())
                                .collect(Collectors.toList());

        return BudgetDTO.BudgetSettingResponse.builder()
                .userId(userId)
                .yearMonth(yearMonth)
                .totalBudget(totalBudget)
                .categoryBudgets(categoryBudgets)
                .build();    

    }

    // 전체 예산 저장
    @Transactional
    public void saveTotalBudget(String userId, String yearMonth, Integer totalBudget) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        BudgetEntity budget = budgetRepository.findTotalBudget(user.getUserId(), yearMonth)
                .orElseGet(() -> {
                    BudgetEntity b = new BudgetEntity();
                    b.setUser(user);
                    b.setYearMonth(yearMonth);
                    return b;
                });

        budget.setBudgetAmount(totalBudget);
        budgetRepository.save(budget);
    }

    // 카테고리별 예산 저장
    @Transactional
    public void saveCategoryBudgets(String userId, String yearMonth, List<BudgetDTO.CategoryBudget> categoryBudgets) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (categoryBudgets == null) return;

        for (BudgetDTO.CategoryBudget cb : categoryBudgets) {
            if (cb.getCategoryId() == null) continue;

            AccountCategoryEntity category = categoryRepository.findById(cb.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다."));

            BudgetEntity budget = budgetRepository
                    .findByUser_UserIdAndCategory_CategoryIdAndYearMonth(user.getUserId(), cb.getCategoryId(), yearMonth)
                    .orElseGet(() -> {
                        BudgetEntity b = new BudgetEntity();
                        b.setUser(user);
                        b.setCategory(category);
                        b.setYearMonth(yearMonth);
                        return b;
                    });

            budget.setBudgetAmount(cb.getBudgetAmount());
            budgetRepository.save(budget);
        }
    }
}
