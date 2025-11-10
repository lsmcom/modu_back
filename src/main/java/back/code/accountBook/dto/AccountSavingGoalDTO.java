package back.code.accountBook.dto;

import java.time.LocalDate;

import back.code.accountBook.entity.AccountSavingsGoalEntity;
import back.code.user.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class AccountSavingGoalDTO {

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class Response {
        private Integer goalId;
        private String goalName;
        private Integer targetAmount;
        private Integer currentAmount;
        private LocalDate startDate;
        private LocalDate endDate;
        private String userId; 

        // 달성률 계산
        public double getProgress() {
            if (currentAmount == null || targetAmount == null || targetAmount == 0) return 0;
            return (double) currentAmount / targetAmount * 100;
        }

        public static Response of(AccountSavingsGoalEntity entity) {
            return Response.builder()
                    .goalId(entity.getGoalId())
                    .goalName(entity.getGoalName())
                    .targetAmount(entity.getTargetAmount())
                    .currentAmount(entity.getCurrentAmount())
                    .startDate(entity.getStartDate())
                    .endDate(entity.getEndDate())
                    .userId(entity.getUser().getUserId())
                    .build();
        }
    }

    // 작성창 >> 저축목표
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class writeGoals {
        private Integer goalId;
        private String goalName;

        public static writeGoals of(AccountSavingsGoalEntity entity) {
            return writeGoals.builder()
                        .goalId(entity.getGoalId())
                        .goalName(entity.getGoalName())
                        .build();
        }
    }

    // 클라이언트 -> 서버
    @Data
    public static class Request {
        private Integer goalId;
        private String goalName;
        private Integer targetAmount;
        private LocalDate startDate;
        private LocalDate endDate;
        private String userId;

        public AccountSavingsGoalEntity to(UserEntity user){
            AccountSavingsGoalEntity goal = new AccountSavingsGoalEntity();
            goal.setGoalId(this.goalId);
            goal.setGoalName(this.goalName);
            goal.setTargetAmount(this.targetAmount);
            goal.setStartDate(this.startDate);
            goal.setEndDate(this.endDate);
            goal.setUser(user);

            return goal;
        }
    }

}
