package back.code.memo.dto;

import lombok.Data;
import java.util.List;

@Data
public class MemoFolderWithMemosDTO {
    private Integer folderId;
    private String folderName;
    private List<MemoDTO> memos;
}
