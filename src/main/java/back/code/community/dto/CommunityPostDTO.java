package back.code.community.dto;

import back.code.community.entity.enum_.ImageSizeType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;

@Getter
public class CommunityPostDTO {
    private Integer postId;
    private Integer boardId;
    private String boardName;
    private String userId;
    private String userNick;

    @Setter
    private String profileImagePath;
    private String title;
    private String contents;
    private Integer readCount;
    private Integer likeCount;
    private Character isTemporary;
    private boolean isLiked;

    @Setter
    private Integer commentCount;
    @Setter
    private String thumbnailPath;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createAt;

    private ImageSizeType imageSizeType;

    /** Projection용 생성자 (Service에서 profileImagePath 생성해서 넣음) */
    public CommunityPostDTO(Integer postId, Integer boardId, String boardName,
                            String userId, String userNick,
                            String profileImagePath,
                            String title, String contents,
                            Integer readCount, Integer likeCount,
                            Character isTemporary, LocalDateTime createAt) {

        this.postId = postId;
        this.boardId = boardId;
        this.boardName = boardName;
        this.userId = userId;
        this.userNick = userNick;
        this.profileImagePath = profileImagePath;
        this.title = title;
        this.contents = contents;
        this.readCount = readCount;
        this.likeCount = likeCount;
        this.isTemporary = isTemporary;
        this.createAt = createAt;
    }

    /** JPQL에서 CASE WHEN EXISTS(...) 대응 생성자 */
    public CommunityPostDTO(Integer postId, Integer boardId, String boardName,
                            String userId, String userNick,
                            String profileImagePath,
                            String title, String contents,
                            Integer readCount, Integer likeCount,
                            Character isTemporary, LocalDateTime createAt,
                            boolean isLiked) {

        this.postId = postId;
        this.boardId = boardId;
        this.boardName = boardName;
        this.userId = userId;
        this.userNick = userNick;
        this.profileImagePath = profileImagePath;
        this.title = title;
        this.contents = contents;
        this.readCount = readCount;
        this.likeCount = likeCount;
        this.isTemporary = isTemporary;
        this.createAt = createAt;
        this.isLiked = isLiked;
    }
}
