package back.code.accountBook.dto;

import back.code.accountBook.entity.BudgetEntity;
import lombok.*;

import java.util.List;

public class BudgetDTO {

    // 월별 예산 설정 응답
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BudgetSettingResponse {
        private String userId;
        private String yearMonth;
        private Integer totalBudget;
        private List<CategoryBudgetInfo> categoryBudgets;
    }

    // 카테고리별 예산 응답
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryBudgetInfo {
        private Integer categoryId;
        private String categoryName;
        private Integer budgetAmount;
    }

    // 임계값 응답
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ThresholdResponse {
        private Integer totalBudgetThreshold;      // 전체 예산 임계값
        private Integer categoryBudgetThreshold;   // 카테고리별 예산 임계값 (공통)
    }

    // 월별 예산 설정 요청
    @Data
    public static class BudgetSettingRequest {
        private String userId;
        private String yearMonth;
        private Integer totalBudget;
        private List<CategoryBudget> categoryBudgets;
    }

    // 카테고리별 예산 요청
    @Data
    public static class CategoryBudget {
        private Integer categoryId;
        private Integer budgetAmount;
    }

    // 전체
    @Data
    public static class TotalBudgetRequest {
        private String userId;
        private String yearMonth;
        private Integer totalBudget;
    }

    // 임계값 요청
    @Data
    public static class ThresholdRequest {
        private String userId;
        private String yearMonth;
        private Integer totalBudgetThreshold;      // 전체 예산 임계값
        private Integer categoryBudgetThreshold;   // 카테고리별 예산 임계값 (공통)
    }

}