package back.code.community.dto;

import back.code.file.entity.FileEntity;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityPostFileDTO {
    private String fileId;
    private String fileName;
    private Long fileSize;
    private String fileType;
    private String filePath;
    private String storedName;
    private String fileThumbName;
    private String fileUrl;

    public static CommunityPostFileDTO from(FileEntity entity) {
        if (entity == null) return null;

        String fileUrl = buildUrl(entity.getFilePath(), entity.getStoredName());

        return CommunityPostFileDTO.builder()
                .fileId(entity.getFileId())
                .fileName(entity.getFileName())
                .fileSize(entity.getFileSize())
                .fileType(entity.getFileType())
                .filePath(entity.getFilePath())
                .storedName(entity.getStoredName())
                .fileThumbName(entity.getFileThumbName())
                .fileUrl(fileUrl)
                .build();
    }

    /** 내부 저장 경로 → 외부 접근 URL 변환 */
    private static String buildUrl(String filePath, String storedName) {
        if (filePath == null || storedName == null) return null;
        String normalized = filePath.replace("\\", "/");
        String relative = normalized.replace("C:/files/modu", ""); // ✅ 로컬 기준
        return "http://localhost:9090" + relative + "/" + storedName;
    }
}
