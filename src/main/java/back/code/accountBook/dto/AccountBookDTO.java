package back.code.accountBook.dto;

import back.code.accountBook.entity.AccountBookEntity;
import lombok.*;

import java.time.LocalDate;

public class AccountBookDTO {

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class Response{

        private int accountBookId;
        private String type;
        private LocalDate date;
        private String method;
        private int amount;
        private String userId;
        private String categoryName;

        public static Response of (AccountBookEntity entity){

            return Response.builder()
                    .accountBookId(entity.getAccountId())
                    .type(entity.getType())
                    .date(entity.getDate())
                    .method(entity.getMethod())
                    .amount(entity.getAmount())
                    .userId(entity.getUserId().getUserId())
                    .categoryName(entity.getCategoryId().getCategoryName())
                    .build();
        }
    }

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class Detail{

        private int accountBookId;
        private String type;
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
                    .method(entity.getMethod())
                    .amount(entity.getAmount())
                    .content(entity.getContent())
                    .userId(entity.getUserId().getUserId())
                    .categoryName(entity.getCategoryId().getCategoryName())
                    .savingGoalName(entity.getGoalId() != null ? entity.getGoalId().getGoalName() : null)
                    .build();
        }
    }

    // 클라이언트 -> 서버
    @Data
    public static class Request {

        private String type;
        private LocalDate date;
        private String method;
        private int amount;
        private String content;
        private String userId;
        private String categoryId;
        private String savingGoalId;
    }

}
