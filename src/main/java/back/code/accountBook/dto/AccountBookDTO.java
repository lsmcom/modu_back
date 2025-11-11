package back.code.accountBook.dto;

import back.code.accountBook.entity.AccountBookEntity;
import back.code.accountBook.entity.AccountCategoryEntity;
import back.code.accountBook.entity.AccountSavingsGoalEntity;
import back.code.accountBook.entity.InstallmentSettingEntity;
import back.code.accountBook.entity.RecurringSettingEntity;
import back.code.accountBook.enums.AccountCycle;
import back.code.accountBook.enums.AccountMethod;
import back.code.accountBook.enums.AccountType;
import back.code.file.dto.FileDTO;
import back.code.user.entity.UserEntity;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class AccountBookDTO {

    // 서버 -> 클라이언트
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class Response{

        private Integer accountBookId;
        private AccountType type;
        private LocalDate date;
        private AccountMethod method;
        private Integer amount;
        private String userId;
        private  Integer categoryId;
        private String categoryName;
        private Integer savingGoalId;
        private String savingGoalName;
        private String content;

        public static Response of (AccountBookEntity entity){
            return Response.builder()
                    .accountBookId(entity.getAccountId())
                    .type(entity.getType())
                    .date(entity.getDate())
                    .method(entity.getMethod())
                    .amount(entity.getAmount())
                    .userId(entity.getUser().getUserId())
                    .categoryId(entity.getCategory().getCategoryId())
                    .categoryName(entity.getCategory().getCategoryName())
                    .savingGoalId(entity.getGoal() != null ? entity.getGoal().getGoalId() : null)
                    .savingGoalName(entity.getGoal() != null ? entity.getGoal().getGoalName() : null)
                    .content(entity.getContent())
                    .build();
        }
    }

    // 상세내용
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class Detail{

        private Integer accountBookId;
        private AccountType type;
        private LocalDate date;
        private AccountMethod method;
        private Integer amount;
        private String content;
        private String userId;
        private Integer categoryId;
        private String categoryName;
        private Integer savingGoalId;
        private String savingGoalName;
        private List<FileDTO> files;
        private RecurringDTO recurring;
        private InstallmentDTO installment;

        public static Detail of (AccountBookEntity entity,
                                 String filePath,
                                 RecurringSettingEntity recurring,
                                 InstallmentSettingEntity installment){

            List<FileDTO> files = entity.getFiles().stream()
                    .map(fm -> FileDTO.from(fm.getFile(), filePath))
                    .collect(Collectors.toList());

            return Detail.builder()
                    .accountBookId(entity.getAccountId())
                    .type(entity.getType())
                    .date(entity.getDate())
                    .method(entity.getMethod())
                    .amount(entity.getAmount())
                    .content(entity.getContent())
                    .userId(entity.getUser().getUserId())
                    .categoryId(entity.getCategory().getCategoryId())
                    .categoryName(entity.getCategory().getCategoryName())
                    .savingGoalId(entity.getGoal() != null ? entity.getGoal().getGoalId() : null)
                    .savingGoalName(entity.getGoal() != null ? entity.getGoal().getGoalName() : null)
                    .files(files)
                    .recurring(recurring != null ? RecurringDTO.of(recurring) : null)
                    .installment(installment != null ? InstallmentDTO.of(installment) : null)
                    .build();
        }
    }

    // 주별 리스트
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class WeekResponse{
        private String weekStartDate;
        private String weekEndDate;  
        private Integer income;
        private Integer expense;

        public static WeekResponse of(AccountProjection projection) {
            return WeekResponse.builder()
                    .weekStartDate(projection.getWeekStart())
                    .weekEndDate(projection.getWeekEnd())
                    .income(projection.getIncome() != null ? projection.getIncome() : 0)
                    .expense(projection.getExpense() != null ? projection.getExpense() : 0)
                    .build();
        }
    }

    // 월별 리스트
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class MonthResponse{
        private String month;
        private Integer income;
        private Integer expense;

        public static MonthResponse of(AccountProjection projection) {
            return MonthResponse.builder()
                    .month(projection.getYearMonth())
                    .income(projection.getIncome() != null ? projection.getIncome() : 0)
                    .expense(projection.getExpense() != null ? projection.getExpense() : 0)
                    .build();
        }
    }

    // 캘린더 리스트
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class CalendarAccountResponse{
        private Integer totalIncome;
        private Integer totalExpense;
        private List<DailyAccount> dailyList;

        // 일별 데이터
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        @Getter
        public static class DailyAccount {
            private String date;
            private Integer income;
            private Integer expense;

            public static DailyAccount of(AccountProjection projection) {
                return DailyAccount.builder()
                        .date(projection.getDate())
                        .income(projection.getIncome() != null ? projection.getIncome() : 0)
                        .expense(projection.getExpense() != null ? projection.getExpense() : 0)
                        .build();
            }
        }
    }

    // 할부내역 리스트
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class InstallmentListResponse {
        private Integer accountBookId;
        private Integer installmentId;
        private String content;
        private String categoryName;
        private AccountMethod method;
        private Integer totalAmount;
        private Integer totalMonths;
        private Integer currentMonth;
        private Integer monthlyAmount;
        private LocalDate startDate;
        private Boolean isCompleted;
        private Integer remainingAmount;

        public static InstallmentListResponse of(AccountBookEntity account,
                                                 InstallmentSettingEntity installment,
                                                 int currentMonth,
                                                 boolean isCompleted, 
                                                 int remainingAmount) {
            return InstallmentListResponse.builder()
                            .accountBookId(account.getAccountId())
                            .installmentId(installment.getInstallmentId())
                            .content(account.getContent())
                            .categoryName(account.getCategory().getCategoryName())
                            .method(account.getMethod())
                            .totalAmount(installment.getTotalAmount())
                            .totalMonths(installment.getTotalMonths())
                            .currentMonth(currentMonth)
                            .monthlyAmount(installment.getMonthlyAmount())
                            .startDate(installment.getStartDate())
                            .isCompleted(isCompleted)
                            .remainingAmount(remainingAmount)
                            .build(); 



        }

    }

    // 클라이언트 -> 서버
    @Data
    public static class Request {
        private Integer accountBookId;
        private AccountType type;
        private LocalDate date;
        private AccountMethod method;
        private Integer amount;
        private String content;
        private String userId;
        private Integer categoryId;
        private Integer savingGoalId;
        private List<String> existingFileIds;
        private RecurringDTO recurring;
        private InstallmentDTO installment;

        public AccountBookEntity to(AccountBookEntity account,
                                    UserEntity user,
                                    AccountCategoryEntity category,
                                    AccountSavingsGoalEntity savingGoal) {
            account.setAccountId(this.accountBookId);
            account.setType(this.type);
            account.setDate(this.date);
            account.setMethod(this.method);
            account.setAmount(this.amount);
            account.setContent(this.content);
            account.setUser(user);
            account.setCategory(category);
            account.setGoal(savingGoal);

            return account;
        }
    }

    // 반복설정
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecurringDTO {

        private Integer recurringId; 
        private AccountCycle cycle; 
        private LocalDate startDate; 
        private LocalDate endDate; 
        private Boolean isActive;
        private String daysOfWeek;
        private LocalDate nextDate; 

        // 응답
        public static RecurringDTO of(RecurringSettingEntity entity) {
            return RecurringDTO.builder()
                    .recurringId(entity.getRecurringId())
                    .cycle(entity.getCycle())
                    .startDate(entity.getStartDate())
                    .endDate(entity.getEndDate())
                    .isActive(entity.getIsActive())
                    .daysOfWeek(entity.getDaysOfWeek())
                    .nextDate(entity.getNextDate())
                    .build();
        }

        // 요청
        public RecurringSettingEntity to(RecurringSettingEntity entity) {
            entity.setCycle(this.cycle);
            entity.setStartDate(this.startDate);
            entity.setEndDate(this.endDate);
            entity.setIsActive(true);
            entity.setDaysOfWeek(this.daysOfWeek);
            entity.setNextDate(this.nextDate); 
            return entity;
        }
    }

    // 할부설정
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InstallmentDTO {

        private Integer installmentId;
        private Integer totalAmount;
        private Integer totalMonths;
        private Integer currentMonth;
        private Integer monthlyAmount;
        private LocalDate startDate;

        // 응답용
        public static InstallmentDTO of(InstallmentSettingEntity entity) {
            return InstallmentDTO.builder()
                    .installmentId(entity.getInstallmentId())
                    .totalAmount(entity.getTotalAmount())
                    .totalMonths(entity.getTotalMonths())
                    .currentMonth(entity.getCurrentMonth())
                    .monthlyAmount(entity.getMonthlyAmount())
                    .startDate(entity.getStartDate())
                    .build();
        }

        // 요청용
        public InstallmentSettingEntity to(InstallmentSettingEntity entity) {
            entity.setTotalAmount(this.totalAmount);
            entity.setTotalMonths(this.totalMonths);
            entity.setCurrentMonth(this.currentMonth);
            entity.setMonthlyAmount(this.monthlyAmount);
            entity.setStartDate(this.startDate);
            return entity;
        }
    }

    // 통계 그래프용
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class chartResponse{

        private Integer accountBookId;
        private String userId;
        private LocalDate date;
        private AccountType type;
        private AccountMethod method;
        private String content;
        private Integer amount;
        private  Integer categoryId;
        private String categoryName;
        private String categoryColor;

        public static chartResponse of (AccountBookEntity entity){
            return chartResponse.builder()
                    .accountBookId(entity.getAccountId())
                    .userId(entity.getUser().getUserId())
                    .date(entity.getDate())
                    .type(entity.getType())
                    .method(entity.getMethod())
                    .content(entity.getContent())
                    .amount(entity.getAmount())
                    .categoryId(entity.getCategory().getCategoryId())
                    .categoryName(entity.getCategory().getCategoryName())
                    .categoryColor(entity.getCategory().getColor())
                    .build();
        }
    }





}
