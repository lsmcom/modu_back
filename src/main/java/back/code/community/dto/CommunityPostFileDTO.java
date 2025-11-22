package back.code.community.dto;

import back.code.file.entity.FileEntity;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;

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

    public static CommunityPostFileDTO from(FileEntity entity, String fileUrl) {
        if (entity == null) return null;

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
}
