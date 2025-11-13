package back.code.community.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunitySearchResponse {
    private Integer postId;
    private String title;
    private String userNick;
    private String boardName;
    private Integer readCount;
    private Integer likeCount;
    private String createAt;
    private String thumbnailPath;
}
