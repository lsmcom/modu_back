package back.code.memo.dto;

import back.code.memo.entity.MemoEntity;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonFormat;

@Data
public class MemoDTO {

    private Integer memoId;          // 메모 고유 ID
    private Integer folderId;        // 폴더 ID (필수)
    private String userId;           // 유저 ID (필수)
    private String memoTitle;        // 메모 제목
    private String memoContents;     // 메모 내용
    private String isFixed;          // 상단 고정 여부 ('Y' / 'N')
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createDate; // 생성일 (DB 자동)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateDate; // 수정일 (DB 자동)

    // 첨부된 파일 리스트
    private List<String> fileIds;
    private List<String> fileThumbnails;

    /* MemoEntity → MemoDTO 변환 메서드 */
    public static MemoDTO from(MemoEntity entity) {
        MemoDTO dto = new MemoDTO();

        dto.setMemoId(entity.getMemoId());
        dto.setMemoTitle(entity.getMemoTitle());

        // 경로 변환 (C:/files → 서버 URL)
        String contents = entity.getMemoContents();
        if (contents != null) {
            contents = contents
                    .replaceAll("/static/imgs/C:/files", "http://localhost:9090/files")
                    .replaceAll("C:/files", "http://localhost:9090/files");
        }
        dto.setMemoContents(contents);

        dto.setIsFixed(entity.getIsFixed());
        dto.setCreateDate(entity.getCreateDate());
        dto.setUpdateDate(entity.getUpdateDate());

        if (entity.getUser() != null)
            dto.setUserId(entity.getUser().getUserId());
        if (entity.getFolder() != null)
            dto.setFolderId(entity.getFolder().getFolderId());

        // 첨부파일 매핑 변환
        if (entity.getMemoFiles() != null && !entity.getMemoFiles().isEmpty()) {
            List<String> fileIds = entity.getMemoFiles().stream()
                    .map(fm -> fm.getMemoFile().getFileId())
                    .collect(Collectors.toList());
            dto.setFileIds(fileIds);

            dto.setFileThumbnails(fileIds.stream()
                    .map(id -> "http://localhost:9090/api/v1/file/" + id + "/thumbnail")
                    .collect(Collectors.toList()));
        }

        return dto;
    }

}
