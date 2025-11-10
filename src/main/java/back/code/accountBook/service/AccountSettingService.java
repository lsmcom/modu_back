package back.code.accountBook.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

    // 저축목표 조회
    @Transactional
    public List<AccountSavingGoalDTO.Response> getGoals(String userId) throws Exception{

        // 사용자 확인
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        // 사용자의 저축 목표 조회
        List<AccountSavingsGoalEntity> goals = savingGoalRepository.findAllByUser(user);
        // DTO 변환
        List<AccountSavingGoalDTO.Response> result = new ArrayList<>();
        for (AccountSavingsGoalEntity goal : goals) {
            result.add(AccountSavingGoalDTO.Response.of(goal));
        }

        return result;
    }

    // 저축목표 추가
    @Transactional
    public AccountSavingGoalDTO.Response writeGoals(AccountSavingGoalDTO.Request request) throws Exception{

        // 사용자 확인
        UserEntity user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        // DTO → 엔티티
        AccountSavingsGoalEntity goals = request.to(user);
        // DTO로 변경
        AccountSavingGoalDTO.Response result = AccountSavingGoalDTO.Response.of(goals);
        // 저장
        savingGoalRepository.save(goals);

        return result;
    }

    // 저축목표 수정
    @Transactional
    public AccountSavingGoalDTO.Response updateGoals(AccountSavingGoalDTO.Request request) throws Exception{

        // 사용자 확인
        UserEntity user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        AccountSavingsGoalEntity goals;
        // 기존 저축목표 수정 및 새 저축목표 생성
        if(request.getGoalId() != null) {
            goals = savingGoalRepository.findById(request.getGoalId())
                    .orElseThrow(() -> new RuntimeException("저축목표를 찾을 수 없습니다."));
            goals.setGoalName(request.getGoalName());
            goals.setTargetAmount(request.getTargetAmount());
            goals.setStartDate(request.getStartDate());
            goals.setEndDate(request.getEndDate());
        } else {
            goals = request.to(user);
        }
        // DTO로 변경
        AccountSavingGoalDTO.Response result = AccountSavingGoalDTO.Response.of(goals);
        // 저장
        savingGoalRepository.save(goals);

        return result;
    }

    // 저축목표 삭제
    @Transactional
    public AccountSavingGoalDTO.Response deleteGoals(String userId,int goalId) throws Exception{

        // 저축목표 확인
        AccountSavingsGoalEntity goals =  savingGoalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("저축목표를 찾을 수 없습니다."));
        // 해당 저축목표가 사용자의 것인지 확인
        if (!goals.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("해당 저축목표에 대한 권한이 없습니다.");
        }
        // DTO로 변경
        AccountSavingGoalDTO.Response result = AccountSavingGoalDTO.Response.of(goals);
        // 저장
        savingGoalRepository.delete(goals);

        return result;
    }

    // 월별 예산 조회
    @Transactional
    public BudgetDTO.BudgetSettingResponse getBudgetSetting(String userId, String yearMonth) throws Exception{

        List<BudgetEntity> budgets = budgetRepository.findAllByUser_UserIdAndYearMonth(userId, yearMonth);

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

        BudgetDTO.BudgetSettingResponse result = BudgetDTO.BudgetSettingResponse.builder()
                .userId(userId)
                .yearMonth(yearMonth)
                .totalBudget(totalBudget)
                .categoryBudgets(categoryBudgets)
                .build();

        return result;
    }

    // 전체 예산 저장
    @Transactional
    public void saveTotalBudget(String userId, String yearMonth, Integer totalBudget) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        BudgetEntity budget = budgetRepository.findByUser_UserIdAndYearMonthAndCategoryIsNull(user.getUserId(), yearMonth)
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

    // 임계값 조회
    @Transactional
    public BudgetDTO.ThresholdResponse getThreshold(String userId, String yearMonth) throws Exception {
        // 전체 예산의 임계값 조회
        Optional<BudgetEntity> totalBudget =
                budgetRepository.findByUser_UserIdAndYearMonthAndCategoryIsNull(userId, yearMonth);

        // 카테고리 예산 임계값 조회
        Optional<BudgetEntity> categoryBudget =
                budgetRepository.findFirstByUser_UserIdAndYearMonthAndCategoryIsNotNull(userId, yearMonth);

        return BudgetDTO.ThresholdResponse.builder()
                .totalBudgetThreshold(totalBudget.map(BudgetEntity::getThreshold).orElse(80))
                .categoryBudgetThreshold(categoryBudget.map(BudgetEntity::getThreshold).orElse(80))
                .build();
    }

    // 전체 임계값 저장
    @Transactional
    public void updateThreshold(BudgetDTO.ThresholdRequest request) throws Exception{

        Integer totalThreshold = request.getTotalBudgetThreshold();

        if (totalThreshold == null) totalThreshold = 80;
        if(totalThreshold > 100) {
            throw new RuntimeException("한도 알림률은 100%를 넘길 수 없습니다.");
        }
        // 전체 예산 임계값 업데이트
        budgetRepository.updateTotalBudgetThreshold(
                request.getUserId(),
                request.getYearMonth(),
                totalThreshold
        );

    }

    // 임계값 저장
    @Transactional
    public void updateCategoryThreshold(BudgetDTO.ThresholdRequest request) throws Exception{

        Integer categoryThreshold = request.getCategoryBudgetThreshold();

        if (categoryThreshold == null) categoryThreshold = 80;
        if(categoryThreshold > 100) {
            throw new RuntimeException("한도 알림률은 100%를 넘길 수 없습니다.");
        }
        // 모든 카테고리별 예산 임계값 일괄 업데이트
        budgetRepository.updateCategoryBudgetThreshold(
                request.getUserId(),
                request.getYearMonth(),
                categoryThreshold
        );
    }
}
