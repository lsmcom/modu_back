package back.code.inquiry.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InquiryDetailResponse {

    private Long inquiryId;
    private String userId;
    private String userNick;
    private String profileImagePath;
    private String title;
    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createAt;
    private boolean isPublic;
    private String status;

    private List<FileDto> files;

    @Getter
    @AllArgsConstructor
    public static class FileDto {
        private String fileId;
        private String fileName;
        private String storedName;
        private String filePath;
    }
}

