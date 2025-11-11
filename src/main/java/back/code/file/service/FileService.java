package back.code.file.service;

import back.code.common.utils.FileUtils;
import back.code.community.repository.CommunityPostFileRepository;
import back.code.file.dto.FileDTO;
import back.code.file.entity.FileEntity;
import back.code.file.repository.FileRepository;
import back.code.user.entity.UserEntity;
import back.code.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    // 다른 도메인 레포지토리 주입
    private final CommunityPostFileRepository communityPostFileRepository;
    // private final MemoFileRepository memoFileRepository; // 나중에 필요하면 추가

    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final FileUtils fileUtils;
    private final WebClient webClient = WebClient.builder()
            .codecs(configurer -> configurer.defaultCodecs()
                    .maxInMemorySize(10 * 1024 * 1024))
            .build();

    // 업로드 할 파일 경로
    @Value("${server.file.upload.path}")
    private String uploadPath;

    /**
     * 일반 파일 업로드
     */
    @Transactional
    public FileDTO uploadFile(MultipartFile file, String userId, String fileType) throws IOException {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

        Map<String, Object> uploadFile = fileUtils.uploadFile(file, uploadPath, fileType);
        String storedName = (String) uploadFile.get("storedFileName");
        String filePath = (String) uploadFile.get("filePath");
        String fileName = (String) uploadFile.get("originalName");

        // 이미지일 경우 썸네일 생성
        String thumbName = null;
        try {
            thumbName = fileUtils.createThumbnail(
                    150, 150,
                    new File(filePath, storedName),
                    Paths.get(filePath, "thumb").toString()
            );
        } catch (Exception e) {
            log.debug("썸네일 생성 스킵 (비이미지 가능): {}", e.getMessage());
        }

        FileEntity entity = new FileEntity();
        entity.setFileId(UUID.randomUUID().toString());
        entity.setUser(user);
        entity.setFileType(fileType.toUpperCase());
        entity.setFileName(fileName);
        entity.setStoredName(storedName);
        entity.setFilePath(filePath);
        entity.setFileSize(file.getSize());
        entity.setFileThumbName(thumbName);

        fileRepository.save(entity);

        log.info("[FILE] 업로드 완료 - userId={}, type={}, file={}", userId, fileType, storedName);

        return FileDTO.from(entity, uploadPath);
    }

    /**
     * URL 기반 파일 업로드 (소셜 프로필)
     */
    @Transactional
    public FileDTO uploadFromUrl(String userId, String imageUrl, String fileType) throws IOException {
        if(imageUrl == null || imageUrl.isBlank()) {
            throw new RuntimeException("이미지 URL이 비어있습니다.");
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

        byte[] bytes = webClient.get().uri(imageUrl).retrieve().bodyToMono(byte[].class).block();

        if (bytes == null || bytes.length == 0) {
            throw new RuntimeException("이미지 다운로드 실패");
        }

        String ext = guessExtension(imageUrl);
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String fullDirPath = Paths.get(uploadPath, fileType, dateDir).toString();

        new File(fullDirPath).mkdirs();

        String rand = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String storedName = rand + "." + ext;
        String fullPath = Paths.get(fullDirPath, storedName).toString();

        try {
            FileOutputStream fos = new FileOutputStream(fullPath);
            fos.write(bytes);
        } catch (Exception e) {
            throw new RuntimeException("파일 저장 실패: " + e.getMessage());
        }

        // 있을 경우 썸네일
        String thumbName = null;
        try {
            thumbName = fileUtils.createThumbnail(
                    150, 150,
                    new File(fullPath),
                    Paths.get(fullDirPath, "thumb").toString()
            );
        } catch (Exception e) {
            log.debug("썸네일 생성 스킵: {}", e.getMessage());
        }

        FileEntity entity = new FileEntity();
        entity.setFileId(UUID.randomUUID().toString());
        entity.setUser(user);
        entity.setFileType(fileType.toUpperCase());
        entity.setFileName(fileType + "_auto." + ext);
        entity.setStoredName(storedName);
        entity.setFilePath(fullDirPath);
        entity.setFileSize((long) bytes.length);
        entity.setFileThumbName(thumbName);

        fileRepository.save(entity);
        log.info("[FILE] URL 업로드 완료 - userId={}, type={}, url={}", userId, fileType, imageUrl);

        return FileDTO.from(entity, uploadPath);
    }

    // 파일 확장자 찾기
    private String guessExtension(String url) {
        String lower = url.toLowerCase();
        if(lower.contains(".png")) return "png";
        if(lower.contains(".webp")) return "webp";
        if(lower.contains(".gif")) return "gif";
        if(lower.contains(".bmp")) return "bmp";
        if(lower.contains(".jpeg")) return "jpeg";
        return "jpg";
    }

    // 파일 삭제
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteFileEntity(FileEntity file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("삭제할 파일 정보가 없습니다.");
        }

        String path = Paths.get(file.getFilePath(), file.getStoredName()).toString();
        String thumbPath = Paths.get(Paths.get(file.getFilePath(), "thumb").toString(),  file.getFileThumbName()).toString();

        try {
            fileUtils.deleteFile(path);
            fileUtils.deleteFile(thumbPath);
            fileRepository.deletePhysicalFile(file.getFileId());
            log.info("[FILE] 삭제 완료: {}", path);
        } catch (IOException e) {
            log.error("[FILE] 파일 삭제 실패: {}", path, e);
            throw e;
        }
    }

    // 고아 파일 삭제
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteFileIfOrphan(String fileId) {
        long refs = communityPostFileRepository.countByFile_FileId(fileId);
        if (refs > 0) {
            log.debug("[CLEANUP] 참조 남음 — 삭제 스킵 fileId={}", fileId);
            return;
        }

        fileRepository.findById(fileId).ifPresent(file -> {
            try {
                // DB 삭제 먼저
                fileRepository.delete(file);
                log.info("[CLEANUP] DB 파일 삭제 완료 fileId={}", fileId);

                // 물리 파일 삭제
                String path = Paths.get(file.getFilePath(), file.getStoredName()).toString();
                fileUtils.deleteFile(path);

                if (file.getFileThumbName() != null) {
                    String thumbPath = Paths.get(file.getFilePath(), "thumb", file.getFileThumbName()).toString();
                    fileUtils.deleteFile(thumbPath);
                }

            } catch (IOException e) {
                log.warn("[CLEANUP] 물리 파일 삭제 실패 fileId={}", fileId, e);
            }
        });
    }

    //파일 아이디로 가져오기
    public FileEntity getFileById(String fileId) {
        return fileRepository.findById(fileId).orElse(null);
    }

    //파일 업로드 후, DB 저장된 FileEntity 객체를 바로 반환하는 메서드.
    @Transactional
    public FileEntity uploadFileAndReturnEntity(MultipartFile multipartFile, String userId, String type) throws IOException {
        // 파일 업로드 후 저장된 FileEntity의 ID를 반환하는 uploadFile() 호출
        FileDTO dto = uploadFile(multipartFile, userId, type);

        // 이미 저장된 파일을 DB에서 조회하여 반환
        return fileRepository.findById(dto.getFileId())
                .orElseThrow(() -> new RuntimeException("파일 업로드 후 조회 실패"));
    }

    /**
     * 파일 다운로드용 ResponseEntity<Resource> 생성
     */
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> downloadFile(String fileId) throws IOException {

        // DB에서 파일 정보 조회
        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 파일입니다."));

        // 실제 파일 경로
        Path filePath = Paths.get(file.getFilePath(), file.getStoredName());
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new RuntimeException("파일을 찾을 수 없거나 읽을 수 없습니다.");
        }

        // 한글 및 공백 파일명 인코딩
        String originalName = file.getFileName() != null ? file.getFileName() : file.getStoredName();
        String encodedName = URLEncoder.encode(originalName, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20"); // 공백 -> %20

        // MIME 타입 자동 감지
        String contentType = Files.probeContentType(filePath);
        if (contentType == null) contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;

        log.info("[FileService] 파일 다운로드 요청 fileId={}, name={}", fileId, originalName);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedName)
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .body(resource);
    }
}
