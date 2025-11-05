package back.code.todo.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class FolderDeleteRequest {

    // TodoContext.js의 folderIdsToDelete Set에 매핑되는, 삭제할 폴더 ID 목록
    private List<Integer> folderIds; // 삭제할 폴더 ID 목록 (필수)
}
