package back.code.accountBook.dto;

import back.code.accountBook.entity.AccountBookEntity;
import back.code.accountBook.enums.AccountMethod;
import back.code.accountBook.enums.AccountType;
import lombok.*;

import java.time.LocalDate;

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
                    .userId(entity.getUserId().getUserId())
                    .categoryName(entity.getCategoryId().getCategoryName())
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

        public static Detail of (AccountBookEntity entity){
            return Detail.builder()
                    .accountBookId(entity.getAccountId())
                    .type(entity.getType())
                    .date(entity.getDate())
                    .method(entity.getMethod().getLabel())
                    .amount(entity.getAmount())
                    .content(entity.getContent())
                    .userId(entity.getUserId().getUserId())
                    .categoryName(entity.getCategoryId().getCategoryName())
                    .savingGoalName(entity.getGoalId() != null ? entity.getGoalId().getGoalName() : null)
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

        public static WeekResponse of(Object[] projection) {
            return WeekResponse.builder()
                    .weekStartDate((String) projection[0])
                    .weekEndDate((String) projection[1])
                    .income(projection[2] != null ? ((Number) projection[2]).intValue() : 0)
                    .expense(projection[3] != null ? ((Number) projection[3]).intValue() : 0)
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

        public static MonthResponse of(Object[] projection) {
            return MonthResponse.builder()
                    .month((String) projection[0])
                    .income(projection[1] != null ? ((Number) projection[1]).intValue() : 0)
                    .expense(projection[2] != null ? ((Number) projection[2]).intValue() : 0)
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

        public static CalendarAccountResponse of(Object[] projection){
            return CalendarAccountResponse.builder()
                                          .totalIncome(projection[0] != null ? ((Number) projection[0]).intValue() : 0)
                                          .totalExpense(projection[1] != null ? ((Number) projection[1]).intValue() : 0)
                                          .build();
        }

    }

    // 클라이언트 -> 서버
    @Data
    public static class Request {

        private AccountType type;
        private LocalDate date;
        private AccountMethod method;
        private int amount;
        private String content;
        private String userId;
        private String categoryId;
        private String savingGoalId;
    }

}
