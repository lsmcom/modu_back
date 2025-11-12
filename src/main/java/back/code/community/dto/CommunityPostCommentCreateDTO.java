package back.code.community.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommunityPostCommentCreateDTO {
    private Integer postId;          // 게시글 번호
    private String contents;         // 내용
    private Integer parentCommentId; // 부모 댓글 번호 (없으면 null)
}
