package back.code.file.dto;

import back.code.file.entity.FileEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private String publicUrl; // 브라우저 접근 URL
    private String thumbPublicUrl; // 썸네일 접근 URL
    private String createAt; // 파일 생성일
    private String updateAt; // 파일 수정일

    /**
     * FileEntity -> FileDTO 변환 (정적 팩토리 메서드)
     */
    public static FileDTO from(FileEntity entity, String uploadPath){
        if (entity == null) return null;

        String publicUrl = toPublicUrl(uploadPath, entity.getFilePath(), entity.getStoredName());
        String thumbUrl = entity.getFileThumbName() != null ? toPublicUrl(uploadPath,
                Paths.get(entity.getFilePath(), "thumb").toString(),
                entity.getFileThumbName()) : null;

        return FileDTO.builder()
                .fileId(entity.getFileId())
                .userId(entity.getUser().getUserId())
                .fileType(entity.getFileType())
                .fileName(entity.getFileName())
                .storedName(entity.getStoredName())
                .filePath(entity.getFilePath())
                .fileSize(entity.getFileSize())
                .fileThumbName(entity.getFileThumbName())
                .publicUrl(publicUrl)
                .thumbPublicUrl(thumbUrl)
                .createAt(entity.getCreateAt() != null ? entity.getCreateAt().toString() : null)
                .updateAt(entity.getUpdateAt() != null ? entity.getUpdateAt().toString() : null)
                .build();
    }

    /* 내부 저장 경로를 외부 접근 URL로 변환 */
    private static String toPublicUrl(String uploadPath, String absDirPath, String storedName) {
        if (uploadPath == null || absDirPath == null) return "";

        // 경로 구분자 통일 및 중복 슬래시 정리
        String normalized = absDirPath.replace("\\", "/").replaceAll("//+", "/");
        String base = uploadPath.replace("\\", "/").replaceAll("//+", "/");

        // 마지막 슬래시 제거
        if (base.endsWith("/")) base = base.substring(0, base.length() - 1);

        // 대소문자 무시 비교 (Windows는 case-insensitive)
        String relative = normalized;
        if (relative.toLowerCase().startsWith(base.toLowerCase())) {
            relative = relative.substring(base.length());
        }

        // 맨 앞 슬래시 제거
        if (relative.startsWith("/")) relative = relative.substring(1);

        // 최종 URL 조립
        String result = "http://localhost:9090/files/" + relative + "/" + storedName;

        // 경로 확인용 임시 로그 (에러나면 경로보고 에러 유추)
        System.out.println("[DEBUG] uploadPath=" + base);
        System.out.println("[DEBUG] absDirPath=" + normalized);
        System.out.println("[DEBUG] result=" + result);

        return result;
    }
}
