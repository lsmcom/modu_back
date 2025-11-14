package back.code.memo.dto;

import back.code.memo.enums.FolderType;
import lombok.Data;

@Data
public class MemoFolderDTO {
    private Integer folderId;
    private String folderName;
    private String userId; // 추가 (폴더 생성 시 사용자 식별용)
    private FolderType folderType;
}
