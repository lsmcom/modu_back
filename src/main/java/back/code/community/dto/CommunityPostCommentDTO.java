package back.code.community.dto;

import back.code.community.entity.CommunityPostCommentEntity;
import back.code.file.entity.FileEntity;
import back.code.file.repository.FileRepository;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
    public static CommunityPostCommentDTO fromEntity(CommunityPostCommentEntity entity, FileRepository fileRepository) {
        String profileImagePath = null;

        List<FileEntity> profileFiles =
                fileRepository.findByUser_UserIdAndFileType(entity.getUser().getUserId(), "PROFILE");
        if (!profileFiles.isEmpty()) {
            FileEntity f = profileFiles.get(0);
            profileImagePath = buildUrl(f.getFilePath(), f.getStoredName());
        }

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

    /** Entity → DTO (대댓글 재귀 포함) */
    public static CommunityPostCommentDTO fromEntityWithReplies(CommunityPostCommentEntity entity,
                                                                FileRepository fileRepository) {
        CommunityPostCommentDTO dto = fromEntity(entity, fileRepository);

        if (entity.getReplies() != null && !entity.getReplies().isEmpty()) {
            dto.setReplies(
                    entity.getReplies().stream()
                            .map(reply -> fromEntityWithReplies(reply, fileRepository))
                            .collect(Collectors.toList())
            );
        }
        return dto;
    }

    private static String buildUrl(String filePath, String storedName) {
        if (filePath == null || storedName == null) return null;
        String normalized = filePath.replace("\\", "/");
        String relative = normalized.replace("C:/files/modu", "");
        return "http://localhost:9090" + relative + "/" + storedName;
    }
}
