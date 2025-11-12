package back.code.community.dto;

import back.code.community.entity.CommunityPostEntity;
import back.code.file.entity.FileEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunityPostDetailDTO {

    private Integer postId;
    private Integer boardId;
    private String boardName;
    private String userId;
    private String userNick;
    private String profileImagePath;
    private String title;
    private String contents;
    private Integer readCount;
    private Integer likeCount;
    private Character isTemporary;
    private String imageSizeType;
    private boolean isLiked;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createAt;
    private List<CommunityPostFileDTO> files;

    /** Entity → DTO 변환 (Service에서 profileImagePath 전달받음) */
    public static CommunityPostDetailDTO fromEntity(CommunityPostEntity post, List<CommunityPostFileDTO> files,
            String profileImagePath) {

        return CommunityPostDetailDTO.builder()
                .postId(post.getPostId())
                .boardId(post.getBoard().getBoardId())
                .boardName(post.getBoard().getBoardName())
                .userId(post.getUser().getUserId())
                .userNick(post.getUser().getUserNick())
                .profileImagePath(profileImagePath)
                .title(post.getTitle())
                .contents(post.getContents())
                .readCount(post.getReadCount())
                .likeCount(post.getLikeCount())
                .isTemporary(post.getIsTemporary())
                .createAt(post.getCreateAt())
                .files(files)
                .build();
    }
}
