package back.code.memo.service;

import back.code.memo.dto.MemoFolderDTO;
import back.code.memo.entity.MemoFolderEntity;
import back.code.memo.enums.FolderType;
import back.code.memo.repository.MemoFolderRepository;
import back.code.user.entity.UserEntity;
import back.code.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemoFolderServiceImpl implements MemoFolderService {

    private final MemoFolderRepository memoFolderRepository;
    private final UserRepository userRepository;

    // 고정된 ID(전체, 기본 폴더)
    private static final Set<Integer> NON_DELETABLE_FOLDER_IDS = Set.of(0, 1);

    @Override
    @Transactional(readOnly = true)
    public List<MemoFolderDTO> getAllFolders() {
        return memoFolderRepository.findAll().stream().map(f -> {
            MemoFolderDTO dto = new MemoFolderDTO();
            dto.setFolderId(f.getFolderId());
            dto.setFolderName(f.getFolderName());
            dto.setFolderType(f.getFolderType());
            dto.setUserId(f.getUser().getUserId());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemoFolderDTO> getUserFolders(String userId) {
        return memoFolderRepository.findByUser_UserId(userId).stream().map(f -> {
            MemoFolderDTO dto = new MemoFolderDTO();
            dto.setFolderId(f.getFolderId());
            dto.setFolderName(f.getFolderName());
            dto.setFolderType(f.getFolderType());
            dto.setUserId(f.getUser().getUserId());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MemoFolderDTO addFolder(MemoFolderDTO dto) {
        UserEntity user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found: " + dto.getUserId()));

        MemoFolderEntity folder = MemoFolderEntity.builder()
                .folderName(dto.getFolderName())
                .user(user)
                .folderType(FolderType.NORMAL)
                .build();

        MemoFolderEntity saved = memoFolderRepository.save(folder);

        MemoFolderDTO result = new MemoFolderDTO();
        result.setFolderId(saved.getFolderId());
        result.setFolderName(saved.getFolderName());
        return result;
    }

    @Override
    @Transactional
    public MemoFolderDTO updateFolder(Integer folderId, MemoFolderDTO dto) {
        MemoFolderEntity folder = memoFolderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("Folder not found: " + folderId));

        if (NON_DELETABLE_FOLDER_IDS.contains(folder.getFolderId())) {
            throw new IllegalArgumentException("기본 폴더는 이름을 수정할 수 없습니다.");
        }

        folder.setFolderName(dto.getFolderName());
        MemoFolderEntity updated = memoFolderRepository.save(folder);

        dto.setFolderId(updated.getFolderId());
        return dto;
    }

    @Override
    @Transactional
    public void deleteFolder(Integer folderId) {

        if (NON_DELETABLE_FOLDER_IDS.contains(folderId)) {
            throw new IllegalArgumentException("기본 폴더(ID: " + folderId + ")는 삭제할 수 없습니다.");
        }

        if (!memoFolderRepository.existsById(folderId)) {
            throw new RuntimeException("Folder not found: " + folderId);
        }

        memoFolderRepository.deleteById(folderId);
    }

    @Override
    @Transactional
    public void createDefaultFolders(UserEntity user) {

        MemoFolderEntity memoFolderEntity = MemoFolderEntity.builder()
            .user(user)
            .folderName("전체")
            .folderType(FolderType.ALL)
            .build();
        memoFolderRepository.save(memoFolderEntity);     

        MemoFolderEntity memoBasicFolderEntity = MemoFolderEntity.builder()
            .user(user)
            .folderName("기본폴더")
            .folderType(FolderType.DEFAULT)
            .build();
        memoFolderRepository.save(memoBasicFolderEntity);     
    }
}
