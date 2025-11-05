package back.code.accountBook.dto;

import back.code.accountBook.entity.AccountBookEntity;
import back.code.accountBook.entity.AccountFileMappingEntity;
import back.code.accountBook.entity.AccountCategoryEntity;
import back.code.accountBook.entity.AccountSavingsGoalEntity;
import back.code.accountBook.enums.AccountMethod;
import back.code.accountBook.enums.AccountType;
import back.code.user.entity.UserEntity;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

public class AccountBookDTO {

    // 서버 -> 클라이언트
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class Response{

        private int accountBookId;
        private AccountType type;
        private LocalDate date;
        private String method;
        private int amount;
        private String userId;
        private String categoryName;
        private String content;

        public static Response of (AccountBookEntity entity){
            return Response.builder()
                    .accountBookId(entity.getAccountId())
                    .type(entity.getType())
                    .date(entity.getDate())
                    .method(entity.getMethod().getLabel())
                    .amount(entity.getAmount())
                    .userId(entity.getUser().getUserId())
                    .categoryName(entity.getCategory().getCategoryName())
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

        private int accountBookId;
        private AccountType type;
        private LocalDate date;
        private String method;
        private int amount;
        private String content;
        private String userId;
        private String categoryName;
        private String savingGoalName;
        private List<AccountFileMappingEntity> fileList;

        public static Detail of (AccountBookEntity entity){



            return Detail.builder()
                    .accountBookId(entity.getAccountId())
                    .type(entity.getType())
                    .date(entity.getDate())
                    .method(entity.getMethod().getLabel())
                    .amount(entity.getAmount())
                    .content(entity.getContent())
                    .userId(entity.getUser().getUserId())
                    .categoryName(entity.getCategory().getCategoryName())
                    .savingGoalName(entity.getGoal() != null ? entity.getGoal().getGoalName() : null)
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
        private int income;
        private int expense;

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
        private int income;
        private int expense;

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
        private int totalIncome;
        private int totalExpense;
        private List<DailyAccount> dailyList;

        // 일별 데이터
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        @Getter
        public static class DailyAccount {
            private String date;
            private int income;
            private int expense;

            public static DailyAccount of(AccountProjection projection) {
                return DailyAccount.builder()
                        .date(projection.getDate())
                        .income(projection.getIncome() != null ? projection.getIncome() : 0)
                        .expense(projection.getExpense() != null ? projection.getExpense() : 0)
                        .build();
            }
        }
    }

    // 클라이언트 -> 서버
    @Data
    public static class Request {
        private int accountBookId;
        private AccountType type;
        private LocalDate date;
        private AccountMethod method;
        private int amount;
        private String content;
        private String userId;
        private int categoryId;
        private Integer savingGoalId;

        public AccountBookEntity to(List<AccountFileMappingEntity> fileEntities,
                                    UserEntity user,
                                    AccountCategoryEntity category,
                                    AccountSavingsGoalEntity savingsGoal) {
            AccountBookEntity accountBook = new AccountBookEntity();
            accountBook.setAccountId(this.accountBookId);
            accountBook.setType(this.type);
            accountBook.setDate(this.date);
            accountBook.setMethod(this.method);
            accountBook.setAmount(this.amount);
            accountBook.setContent(this.content);
            accountBook.setUser(user);
            accountBook.setCategory(category);
            accountBook.setGoal(savingsGoal);

            return accountBook;
        }
    }

}
