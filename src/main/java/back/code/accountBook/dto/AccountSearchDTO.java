package back.code.accountBook.dto;

import java.time.LocalDate;

import back.code.accountBook.entity.AccountBookEntity;
import back.code.accountBook.enums.AccountMethod;
import back.code.accountBook.enums.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class AccountSearchDTO {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountSearchResultDTO {
        private Integer accountId; 
        private LocalDate date;  
        private AccountType type;    
        private AccountMethod method; 
        private String content; 
        private String category;     
        private Integer amount; 

        public static AccountSearchResultDTO of(AccountBookEntity entity) {
            return AccountSearchResultDTO.builder()
                    .accountId(entity.getAccountId())
                    .date(entity.getDate())
                    .type(entity.getType())
                    .method(entity.getMethod())
                    .content(entity.getContent())
                    .category(entity.getCategory() != null ? entity.getCategory().getCategoryName() : null)
                    .amount(entity.getAmount())
                    .build();
        }
    }
    
    @Data
    public static class Request {
        private String userId;
        private String keyword;  
        private String startDate; 
        private String endDate;      
        private String method;     
        private String category;      
        private Integer minAmount;     
        private Integer maxAmount;   
        
    }

}
