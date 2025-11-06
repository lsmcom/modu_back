package back.code.community.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommunityPostFileDTO {
    private String fileId;
    private String fileName;
    private Long fileSize;
    private String fileType;
    private String filePath;
    private String storedName;
    private String fileThumbName;
}
