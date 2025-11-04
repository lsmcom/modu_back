package back.code.file.controller;

import back.code.common.dto.ApiResponse;
import back.code.file.dto.FileDTO;
import back.code.file.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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
}
