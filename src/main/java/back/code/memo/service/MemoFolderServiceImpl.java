package back.code.memo.service;

import back.code.memo.dto.MemoFolderDTO;
import back.code.memo.entity.MemoFolderEntity;
import back.code.memo.repository.MemoFolderRepository;
import back.code.user.entity.UserEntity;
import back.code.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemoFolderServiceImpl implements MemoFolderService {

    private final MemoFolderRepository memoFolderRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MemoFolderDTO> getAllFolders() {
        return memoFolderRepository.findAll().stream().map(f -> {
            MemoFolderDTO dto = new MemoFolderDTO();
            dto.setFolderId(f.getFolderId());
            dto.setFolderName(f.getFolderName());
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

        folder.setFolderName(dto.getFolderName());
        MemoFolderEntity updated = memoFolderRepository.save(folder);

        dto.setFolderId(updated.getFolderId());
        return dto;
    }

    @Override
    @Transactional
    public void deleteFolder(Integer folderId) {
        if (!memoFolderRepository.existsById(folderId)) {
            throw new RuntimeException("Folder not found: " + folderId);
        }
        memoFolderRepository.deleteById(folderId);
    }
}
