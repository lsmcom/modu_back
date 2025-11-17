package back.code.community.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyPostActivityDTO {

    private Integer postId;
    private Integer boardId;
    private String boardName;

    private String title;
    private String contents;

    private Integer readCount;
    private Integer likeCount;
    private Integer commentCount;

    private String thumbnailPath;   // 첫 번째 이미지 썸네일 (없으면 null)

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createAt;
}
