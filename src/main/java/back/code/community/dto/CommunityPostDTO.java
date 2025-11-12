package back.code.community.dto;

import back.code.community.entity.enum_.ImageSizeType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommunityPostDTO {
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
    private boolean isLiked;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createAt;

    private ImageSizeType imageSizeType;

    // Projection용 생성자
    public CommunityPostDTO(Integer postId, Integer boardId, String boardName,
                            String userId, String userNick,
                            String filePath, String storedName,
                            String title, String contents,
                            Integer readCount, Integer likeCount,
                            Character isTemporary, LocalDateTime createAt) {

        this.postId = postId;
        this.boardId = boardId;
        this.boardName = boardName;
        this.userId = userId;
        this.userNick = userNick;
        this.profileImagePath = buildUrl(filePath, storedName);
        this.title = title;
        this.contents = contents;
        this.readCount = readCount;
        this.likeCount = likeCount;
        this.isTemporary = isTemporary;
        this.createAt = createAt;
    }

    // JPQL에서 CASE WHEN EXISTS(...) 대응 생성자
    public CommunityPostDTO(Integer postId, Integer boardId, String boardName,
                            String userId, String userNick,
                            String filePath, String storedName,
                            String title, String contents,
                            Integer readCount, Integer likeCount,
                            Character isTemporary, LocalDateTime createAt,
                            boolean isLiked) {

        this.postId = postId;
        this.boardId = boardId;
        this.boardName = boardName;
        this.userId = userId;
        this.userNick = userNick;
        this.profileImagePath = buildUrl(filePath, storedName);
        this.title = title;
        this.contents = contents;
        this.readCount = readCount;
        this.likeCount = likeCount;
        this.isTemporary = isTemporary;
        this.createAt = createAt;
        this.isLiked = isLiked;
    }

    private static String buildUrl(String filePath, String storedName) {
        if (filePath == null || storedName == null) return null;
        String normalized = filePath.replace("\\", "/");
        String relative = normalized.replace("C:/files/modu", "");
        return "http://localhost:9090" + relative + "/" + storedName;
    }
}
