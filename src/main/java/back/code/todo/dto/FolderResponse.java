package back.code.todo.dto;

import back.code.todo.entity.TodoFolder;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class FolderResponse {

    private Integer folderId;       // 폴더 고유 ID
    private String userId;          // 사용자 ID
    private String name;            // 폴더 이름

    // 엔티티를 기반으로 DTO를 생성하는 편의 메소드
    public static FolderResponse fromEntity(TodoFolder folder) {
        return FolderResponse.builder()
                .folderId(folder.getFolderId())
                .userId(folder.getUserId())
                .name(folder.getName())
                .build();
    }
}