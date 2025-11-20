package back.code.accountBook.scheduler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import back.code.accountBook.entity.AccountSavingsGoalEntity;
import back.code.accountBook.entity.BudgetEntity;
import back.code.accountBook.repository.AccountBookRepository;
import back.code.accountBook.repository.BudgetRepository;
import back.code.accountBook.repository.SavingGoalRepository;
import back.code.notice.entity.Notification;
import back.code.notice.repository.NotificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AccountBookNotificationScheduler {
    
    private final NotificationRepository notificationRepository;
    private final BudgetRepository budgetRepository;
    private final AccountBookRepository accountBookRepository;
    private final SavingGoalRepository savingGoalRepository;
    
    // 30초에 한번 실행
    @Transactional
    @Scheduled(cron = "0,30 * * * * *")
    public void checkBudgetAlerts() throws Exception{
        String currentYearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

        // 전체 사용자 조회
        List<String> userIds = budgetRepository.findDistinctUserIds();

        for (String userId : userIds) {

            // 전체 예산
            List<BudgetEntity> totalBudgets =
                    budgetRepository.findAllByUser_UserIdAndYearMonth(userId, currentYearMonth)
                            .stream()
                            .filter(b -> b.getCategory() == null)
                            .toList();

            for (BudgetEntity budget : totalBudgets) {
                handleTotalBudgetAlert(budget, currentYearMonth);
            }

            // 카테고리별 예산
            List<BudgetEntity> categoryBudgets =
                    budgetRepository.findAllByUser_UserIdAndYearMonth(userId, currentYearMonth)
                            .stream()
                            .filter(b -> b.getCategory() != null)
                            .toList();

            for (BudgetEntity budget : categoryBudgets) {
                handleCategoryBudgetAlert(budget, currentYearMonth);
            }
        }

    }
    
    // 전체 예산 알림
    private void handleTotalBudgetAlert(BudgetEntity budget, String yearMonth) {
        Integer usedExpense = accountBookRepository
                .sumTotalExpenseByUserAndYearMonth(budget.getUser().getUserId(), yearMonth);

        if (usedExpense == null || budget.getBudgetAmount() == null || budget.getBudgetAmount() == 0) return;

        // threshold null 방어
        Integer threshold = budget.getThreshold();
        if (threshold == null || threshold == 0) {
            return;
        }

        double percent = (double) usedExpense / budget.getBudgetAmount() * 100;

        if (percent >= budget.getThreshold()) {
            // 이미 알림 보냈는지 체크
            boolean alreadySent = notificationRepository.existsByUserIdAndTypeAndContentContaining(
                budget.getUser().getUserId(),
                Notification.NotificationType.account_budget,
                String.format("이번 달 예산 %d%% 사용하였습니다.", threshold)
            );

            if (alreadySent) return;

            Notification noti = new Notification();
            noti.setUserId(budget.getUser().getUserId());
            noti.setType(Notification.NotificationType.account_budget);
            noti.setTitle("[가계부] 예산 한도 도달");
            noti.setContent(String.format("이번 달 예산 %d%% 사용하였습니다.", budget.getThreshold()));
            noti.setIsRead(false);
            noti.setCreateDate(LocalDateTime.now());

            notificationRepository.save(noti);
        }
    }
    
    // 카테고리별 예산 알림
    private void handleCategoryBudgetAlert(BudgetEntity budget, String yearMonth) {
        Integer categoryExpense = accountBookRepository
                .sumExpenseByUserAndYearMonthAndCategory(
                        budget.getUser().getUserId(),
                        yearMonth,
                        budget.getCategory().getCategoryId()
                );

        if (categoryExpense == null || budget.getBudgetAmount() == null || budget.getBudgetAmount() == 0) return;

        Integer threshold = budget.getThreshold();
        if (threshold == null || threshold == 0) {
            return;
        }

        double percent = (double) categoryExpense / budget.getBudgetAmount() * 100;

        if (percent >= budget.getThreshold()) {
            String categoryName = budget.getCategory().getCategoryName();
            // 이미 알림 보냈는지 체크
            boolean alreadySent = notificationRepository.existsByUserIdAndTypeAndContentContaining(
                budget.getUser().getUserId(),
                Notification.NotificationType.account_budget,
                String.format("'%s' 카테고리 예산 %d%% 사용했습니다.", categoryName, threshold)
            );

            if (alreadySent) return;

            Notification noti = new Notification();
            noti.setUserId(budget.getUser().getUserId());
            noti.setType(Notification.NotificationType.account_budget); 
            noti.setTitle("[가계부] 카테고리 예산 한도 도달");
            noti.setContent(String.format("'%s' 카테고리 예산 %d%% 사용했습니다.", categoryName, budget.getThreshold()));
            noti.setIsRead(false);
            noti.setCreateDate(LocalDateTime.now());

            notificationRepository.save(noti);
        }
    }

    // 저축 목표 체크
    @Transactional
    @Scheduled(cron = "0,30 * * * * *")
    public void checkSavingsGoalAlerts() throws Exception{
        List<AccountSavingsGoalEntity> allGoals = savingGoalRepository.findAll();
            
        for (AccountSavingsGoalEntity goal : allGoals) {

            // 종료일이 지났으면 스킵
            if (goal.getEndDate() != null && goal.getEndDate().isBefore(LocalDate.now())) {
                continue;
            }   
            checkAndSendSavingsGoalAlert(goal);
        }
    }
    
    // 저축 목표 달성 체크
    private void checkAndSendSavingsGoalAlert(AccountSavingsGoalEntity goal) {
        String userId = goal.getUser().getUserId();
        String goalName = goal.getGoalName();
        
        // 이미 달성 알림 보냈는지 체크
        boolean alreadySent = notificationRepository.existsByUserIdAndTypeAndContentContaining(
            userId,
            Notification.NotificationType.account_goals,
            goalName
        );
        
        if (alreadySent) {
            return;
        }
        
        // 달성률 계산
        double progress = 0;
        if (goal.getCurrentAmount() != null && goal.getTargetAmount() != null && goal.getTargetAmount() != 0) {
            progress = (double) goal.getCurrentAmount() / goal.getTargetAmount() * 100;
        }
        
        // 100% 달성 시 알림
        if (progress >= 100.0) {
            sendSavingsGoalAlert(userId, goalName);
        }
    }

    // 저축 목표 알림
    private void sendSavingsGoalAlert(String userId, String goalName) {
        Notification noti = new Notification();
        noti.setUserId(userId);
        noti.setType(Notification.NotificationType.account_goals);
        noti.setTitle("[가계부] 저축 목표 달성");
        noti.setContent(String.format("저축 목표 '%s' 100%% 달성했습니다!", goalName));
        noti.setIsRead(false);
        noti.setCreateDate(LocalDateTime.now());

        notificationRepository.save(noti);
    }
}