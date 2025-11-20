package back.code.calendar.service;


import back.code.calendar.dto.CalendarFolderRequest;
import back.code.calendar.entity.CalendarFolderEntity;
import back.code.calendar.repository.CalendarFolderRepository;
import back.code.user.entity.UserEntity;
import back.code.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CalendarFolderService {

    private final CalendarFolderRepository folderRepository;
    private final UserRepository userRepository;

    /** 폴더 생성 */
    public CalendarFolderEntity createFolder(CalendarFolderRequest req) {

        // userId로 실제 UserEntity 조회
        UserEntity user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        String type = (req.getFolderType() == null || req.getFolderType().trim().isEmpty())
                ? "PERSONAL"
                : req.getFolderType();

        // 엔티티 생성 시 UserEntity 주입
        CalendarFolderEntity folder = CalendarFolderEntity.builder()
                .user(user)
                .folderName(req.getFolderName())
                .folderType(type)
                .build();

        return folderRepository.save(folder);
    }

    // 회원가입 시 기본 폴더 / 공유 폴더 생성
    @Transactional
    public void createDefaultFolders(UserEntity user) {

        // 기본 폴더 생성
        CalendarFolderEntity personalFolder = CalendarFolderEntity.builder()
                .user(user)
                .folderName("기본 폴더")
                .folderType("PERSONAL")
                .build();
        folderRepository.save(personalFolder);

        // 공유 폴더 생성
        CalendarFolderEntity sharedFolder = CalendarFolderEntity.builder()
                .user(user)
                .folderName("공유 폴더")
                .folderType("SHARED")
                .build();
        folderRepository.save(sharedFolder);
    }

    /** 사용자별 폴더 목록 조회 */
    @Transactional(readOnly = true)
    public List<CalendarFolderEntity> getFoldersByUser(String userId) {
        return folderRepository.findByUser_UserId(userId);
    }

    /** 폴더 단건 조회 */
    @Transactional(readOnly = true)
    public Optional<CalendarFolderEntity> getFolder(Long folderId) {
        return folderRepository.findById(folderId);
    }

    /** 폴더명 수정 */
    public CalendarFolderEntity updateFolder(Long folderId, String newName) {

        CalendarFolderEntity folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 폴더입니다."));
        if ("SHARED".equalsIgnoreCase(folder.getFolderType())) {
            throw new IllegalArgumentException("공유 폴더는 이름을 변경할 수 없습니다.");
        }
        folder.setFolderName(newName);
        return folderRepository.save(folder);
    }

    /** 폴더 삭제 */
    public void deleteFolder(Long folderId) {
        CalendarFolderEntity folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다."));

        if ("SHARED".equalsIgnoreCase(folder.getFolderType())) {
            throw new IllegalArgumentException("공유 폴더는 삭제할 수 없습니다.");
        }

        folderRepository.delete(folder);
    }
}
