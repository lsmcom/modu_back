package back.code.memo.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MemoDTO {

    private Integer memoId;          // 메모 고유 ID
    private Integer folderId;        // 폴더 ID (필수)
    private String userId;           // 유저 ID (필수)
    private String memoTitle;        // 메모 제목
    private String memoContents;     // 메모 내용
    private String isFixed;          // 상단 고정 여부 ('Y' / 'N')
    private LocalDateTime createDate; // 생성일 (DB 자동)
    private LocalDateTime updateDate; // 수정일 (DB 자동)

    // 첨부된 파일 리스트
    private List<String> fileIds;

}
