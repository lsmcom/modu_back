package back.code.common.utils;

import com.mortennobel.imagescaling.AdvancedResizeOp;
import com.mortennobel.imagescaling.MultiStepRescaleOp;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 파일 업로드, 삭제, 썸네일 생성 유틸리티 클래스.
 *
 * <p>이미지 파일만 업로드 가능하며, 디렉토리 미존재 시 자동 생성한다.</p>
 */
@Component
public class FileUtils {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "gif", "png", "webp", "bmp");

    /**
     * 파일 업로드
     *
     * @param file 업로드할 파일
     * @param filePath 저장 경로
     * @return 업로드 결과 정보(Map)
     */
    public Map<String, Object> uploadFile(MultipartFile file, String filePath, String fileType) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        // 하위 폴더 생성 (종류별)
        String typeDirPath = Paths.get(filePath, fileType).toString();
        File typeDir = new File(typeDirPath);
        if (!typeDir.exists()) typeDir.mkdirs();

        // 실행 파일 업로드 방지(.exe, .bat, .sh, .elf, .bin)
        String contentType = file.getContentType();
        if (contentType == null ||
                contentType.contains("x-msdownload") ||
                contentType.contains("application/x-msdos-program") ||
                contentType.contains("application/x-executable") ||
                contentType.contains("application/x-sh")) {
            throw new IllegalArgumentException("실행 파일 업로드는 허용되지 않습니다.");
        }

        String fileName = Objects.requireNonNull(file.getOriginalFilename());
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);

        // 날짜별 하위 폴더 (fileType 안쪽에)
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String fullDirPath = Paths.get(filePath, fileType, dateDir).toString();

        File dir = new File(fullDirPath);
        if (!dir.exists()) dir.mkdirs();

        // 파일명 랜덤 생성
        String randName = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String storedFileName = randName + "." + extension;
        String fullPath = Paths.get(fullDirPath, storedFileName).toString();

        // 파일 저장
        file.transferTo(new File(fullPath));

        // 업로드 결과 반환
        Map<String, Object> result = new HashMap<>();
        result.put("originalName", fileName);
        result.put("storedFileName", storedFileName);
        result.put("filePath", fullDirPath);
        result.put("extension", extension);
        return result;
    }

    /**
     * 파일 삭제
     */
    public void deleteFile(String filePath) throws IOException {
        File deleteFile = new File(filePath);
        if (deleteFile.exists() && !deleteFile.delete()) {
            throw new IOException("파일 삭제 실패: " + filePath);
        }
    }

    /**
     * 이미지 썸네일 생성
     *
     * @param width   썸네일 가로 크기
     * @param height  썸네일 세로 크기
     * @param originFile 원본 이미지 파일
     * @param thumbPath 썸네일 저장 경로
     * @return 생성된 썸네일 파일명
     */
    public String createThumbnail(int width, int height, File originFile, String thumbPath) throws IOException {
        String fileName = originFile.getName();
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("지원하지 않는 이미지 형식입니다: " + extension);
        }

        BufferedImage originImage = ImageIO.read(originFile);
        if (originImage == null) throw new IOException("이미지 파일이 아닙니다.");

        MultiStepRescaleOp scaleOp = new MultiStepRescaleOp(width, height);
        scaleOp.setUnsharpenMask(AdvancedResizeOp.UnsharpenMask.Soft);
        BufferedImage resizedImage = scaleOp.filter(originImage, null);

        String randName = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String thumbFileName = randName + "." + extension;
        String fullPath = Paths.get(thumbPath, thumbFileName).toString();

        File thumbDir = new File(thumbPath);
        if (!thumbDir.exists()) thumbDir.mkdirs();

        boolean written = ImageIO.write(resizedImage, extension, new File(fullPath));
        if (!written) throw new IOException("썸네일 생성 실패");

        return thumbFileName;
    }
}
