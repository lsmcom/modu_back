package back.code.file.controller;

import back.code.common.dto.ApiResponse;
import back.code.file.dto.FileDTO;
import back.code.file.entity.FileEntity;
import back.code.file.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/file")
public class FileRestController {

    private final FileService fileService;

    /** 일반 파일 업로드 */
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<FileDTO>> uploadFile(@RequestParam("file") MultipartFile file,
            @RequestParam("userId") String userId, @RequestParam("fileType") String fileType) throws IOException {
        FileDTO dto = fileService.uploadFile(file, userId, fileType);
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    /** 외부 URL 업로드 (소셜 프로필 등) */
    @PostMapping("/upload/url")
    public ResponseEntity<ApiResponse<FileDTO>> uploadFileFromUrl(@RequestParam("userId") String userId,
            @RequestParam("imageUrl") String imageUrl, @RequestParam("fileType") String fileType) throws IOException {
        FileDTO dto = fileService.uploadFromUrl(userId, imageUrl, fileType);
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    //썸네일 불러오기
    @GetMapping("/{fileId}/thumbnail")
    public ResponseEntity<Resource> getFileThumbnail(@PathVariable String fileId) {
        try {
            // 파일 정보 가져오기
            FileEntity file = fileService.getFileById(fileId);
            if (file == null) {
                return ResponseEntity.notFound().build();
            }

            // 썸네일 있으면 thumb 폴더, 없으면 원본 경로 사용
            Path filePath;
            if (file.getFileThumbName() != null) {
                filePath = Paths.get(file.getFilePath(), "thumb", file.getFileThumbName());
            } else {
                filePath = Paths.get(file.getFilePath(), file.getStoredName());
            }

            FileSystemResource resource = new FileSystemResource(filePath);
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(filePath);
            if (contentType == null) contentType = "application/octet-stream";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
