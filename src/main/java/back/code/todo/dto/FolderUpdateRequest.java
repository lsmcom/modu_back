package back.code.todo.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FolderUpdateRequest {

    // TodoContext.js의 editingFolder.folder_id에 매핑
    private Integer folderId;       // 수정할 폴더 ID (필수)

    // TodoContext.js의 name에 매핑
    private String name;            // 새 폴더 이름 (필수)
}