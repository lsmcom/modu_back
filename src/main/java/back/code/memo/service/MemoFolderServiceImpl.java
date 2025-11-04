package back.code.memo.service;

import back.code.memo.dto.MemoFolderDTO;
import back.code.memo.entity.MemoFolderEntity;
import back.code.memo.repository.MemoFolderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemoFolderServiceImpl implements MemoFolderService {

    private final MemoFolderRepository memoFolderRepository;

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
    @Transactional
    public MemoFolderDTO addFolder(MemoFolderDTO dto) {
        MemoFolderEntity entity = MemoFolderEntity.builder()
                .folderName(dto.getFolderName())
                .build();

        MemoFolderEntity saved = memoFolderRepository.save(entity);

        dto.setFolderId(saved.getFolderId());
        return dto;
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

