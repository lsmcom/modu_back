package back.code.file.dto;

import back.code.file.entity.FileEntity;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;

import java.nio.file.Paths;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileDTO {

    private String fileId; // 파일 번호
    private String userId; // 회원 아이디
    private String fileType; // 파일 종류
    private String fileName; // 원본 파일명
    private String storedName; // 파일 저장 이름
    private String filePath; // 파일 경로
    private Long fileSize; // 파일 크기
    private String fileThumbName; // 썸네일 파일명

    @Setter
    private String publicUrl; // 브라우저 접근 URL
    @Setter
    private String thumbPublicUrl; // 썸네일 접근 URL

    private String createAt; // 파일 생성일
    private String updateAt; // 파일 수정일

    /**
     * FileEntity -> FileDTO 변환 (정적 팩토리 메서드)
     */
    public static FileDTO from(FileEntity entity){
        if (entity == null) return null;

        return FileDTO.builder()
                .fileId(entity.getFileId())
                .userId(entity.getUser().getUserId())
                .fileType(entity.getFileType())
                .fileName(entity.getFileName())
                .storedName(entity.getStoredName())
                .filePath(entity.getFilePath())
                .fileSize(entity.getFileSize())
                .fileThumbName(entity.getFileThumbName())
                .createAt(entity.getCreateAt() != null ? entity.getCreateAt().toString() : null)
                .updateAt(entity.getUpdateAt() != null ? entity.getUpdateAt().toString() : null)
                .build();
    }
}
