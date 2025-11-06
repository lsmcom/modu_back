package back.code.accountBook.dto;

import back.code.accountBook.entity.AccountBookEntity;
import back.code.accountBook.entity.AccountCategoryEntity;
import back.code.accountBook.entity.AccountSavingsGoalEntity;
import back.code.accountBook.enums.AccountMethod;
import back.code.accountBook.enums.AccountType;
import back.code.file.dto.FileDTO;
import back.code.user.entity.UserEntity;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

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

        private int accountBookId;
        private AccountType type;
        private LocalDate date;
        private AccountMethod method;
        private int amount;
        private String userId;
        private  int categoryId;
        private String categoryName;
        private Integer savingsGoalId;
        private String savingsGoalName;
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
                    .savingsGoalId(entity.getGoal() != null ? entity.getGoal().getGoalId() : null)
                    .savingsGoalName(entity.getGoal() != null ? entity.getGoal().getGoalName() : null)
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
        private AccountMethod method;
        private int amount;
        private String content;
        private String userId;
        private String categoryName;
        private String savingGoalName;
        private List<FileDTO> files;

        public static Detail of (AccountBookEntity entity,String filePath){

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
                    .categoryName(entity.getCategory().getCategoryName())
                    .savingGoalName(entity.getGoal() != null ? entity.getGoal().getGoalName() : null)
                    .files(files)
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
        private List<MultipartFile> files;

        public AccountBookEntity to(AccountBookEntity account,
                                    UserEntity user,
                                    AccountCategoryEntity category,
                                    AccountSavingsGoalEntity savingsGoal) {
            account.setAccountId(this.accountBookId);
            account.setType(this.type);
            account.setDate(this.date);
            account.setMethod(this.method);
            account.setAmount(this.amount);
            account.setContent(this.content);
            account.setUser(user);
            account.setCategory(category);
            account.setGoal(savingsGoal);

            return account;
        }
    }

}
