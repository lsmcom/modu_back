package back.code.accountBook.dto;

import back.code.accountBook.entity.AccountCategoryEntity;
import back.code.accountBook.enums.AccountType;
import back.code.user.entity.UserEntity;
import lombok.*;

public class AccountCategoryDTO {

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class Response {
        private Integer categoryId;
        private String categoryName;
        private AccountType type;
        private Boolean isDefault;
        private String userId;
        private String color;

        public static Response of(AccountCategoryEntity entity) {
            return Response.builder()
                    .categoryId(entity.getCategoryId())
                    .categoryName(entity.getCategoryName())
                    .type(entity.getType())
                    .isDefault(entity.getIsDefault())
                    .userId(entity.getUser() != null ? entity.getUser().getUserId() : null)
                    .color(entity.getColor())
                    .build();
        }
    }

    // 클라이언트 -> 서버
    @Data
    public static class Request {
        private Integer categoryId;
        private String categoryName;
        private AccountType type;
        private Boolean isDefault;
        private String userId;
        private String color;

        public AccountCategoryEntity to(UserEntity user){
            AccountCategoryEntity category = new AccountCategoryEntity();
            category.setCategoryId(this.categoryId);
            category.setCategoryName(this.categoryName);
            category.setType(this.type);
            category.setIsDefault(this.isDefault);
            category.setUser(user);
            category.setColor(color);

            return category;
        }
    }

}
