package back.code.community.dto;

import back.code.community.entity.CommunityPostCommentEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunityPostCommentDTO {
    private Integer commentId;
    private Integer postId;
    private String userId;
    private String userNick;
    private String profileImagePath;
    private String contents;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateAt;

    private Integer parentCommentId; // 부모 댓글 ID (null이면 일반 댓글)
    private List<CommunityPostCommentDTO> replies; // 대댓글 리스트

    /** Entity → DTO (단일) */
    public static CommunityPostCommentDTO fromEntity(CommunityPostCommentEntity entity, String profileImagePath) {

        return CommunityPostCommentDTO.builder()
                .commentId(entity.getCommentId())
                .postId(entity.getPost().getPostId())
                .userId(entity.getUser().getUserId())
                .userNick(entity.getUser().getUserNick())
                .profileImagePath(profileImagePath)
                .contents(entity.getContents())
                .createAt(entity.getCreateAt())
                .updateAt(entity.getUpdateAt())
                .parentCommentId(entity.getParentComment() != null ? entity.getParentComment().getCommentId() : null)
                .build();
    }
}
