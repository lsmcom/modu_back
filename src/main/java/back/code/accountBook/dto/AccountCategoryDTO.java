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
        private int categoryId;
        private String categoryName;
        private AccountType type;
        private boolean isDefault;
        private String userId;

        public static Response of(AccountCategoryEntity entity) {
            return Response.builder()
                    .categoryId(entity.getCategoryId())
                    .categoryName(entity.getCategoryName())
                    .type(entity.getType())
                    .isDefault(entity.isDefault())
                    .userId(entity.getUser() != null ? entity.getUser().getUserId() : null)
                    .build();
        }
    }

    // 클라이언트 -> 서버
    @Data
    public static class Request {
        private int categoryId;
        private String categoryName;
        private AccountType type;
        private boolean isDefault;
        private String userId;

        public AccountCategoryEntity to(UserEntity user){
            AccountCategoryEntity category = new AccountCategoryEntity();
            category.setCategoryId(this.categoryId);
            category.setCategoryName(this.categoryName);
            category.setType(this.type);
            category.setDefault(this.isDefault);
            category.setUser(user);

            return category;
        }
    }

}
