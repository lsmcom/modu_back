package back.code.community.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyCommentActivityDTO {

    private Integer commentId;

    private Integer postId;
    private String postTitle;   // 댓글이 달린 게시글 제목

    private String contents;    // 댓글 내용

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createAt;
}
