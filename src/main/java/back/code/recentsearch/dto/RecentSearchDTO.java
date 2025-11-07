package back.code.recentsearch.dto;

import back.code.recentsearch.entity.RecentSearchEntity;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecentSearchDTO {

    private Long id;
    private String userId;
    private String type;
    private String keyword;

    public static RecentSearchDTO fromEntity(RecentSearchEntity entity) {
        return RecentSearchDTO.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .type(entity.getType())
                .keyword(entity.getKeyword())
                .build();
    }
}
