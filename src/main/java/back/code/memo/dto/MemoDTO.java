package back.code.memo.dto;

import lombok.Data;

@Data
public class MemoDTO {
    private Integer memoId;
    private String memoTitle;
    private String memoContents;
    private String isFixed;
    private Integer folderId;
}
