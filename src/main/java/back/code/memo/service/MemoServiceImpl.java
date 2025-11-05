package back.code.memo.service;

import back.code.file.service.FileService;
import back.code.memo.dto.MemoDTO;
import back.code.memo.dto.MemoFolderDTO;
import back.code.memo.dto.MemoFolderWithMemosDTO;
import back.code.memo.dto.MoveFolderRequest;
import back.code.memo.entity.MemoEntity;
import back.code.memo.entity.MemoFolderEntity;
import back.code.memo.repository.MemoRepository;
import back.code.memo.repository.MemoFolderRepository;
import back.code.user.entity.UserEntity;
import back.code.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemoServiceImpl implements MemoService {

    private final MemoRepository memoRepository;
    private final MemoFolderRepository memoFolderRepository;
    private final UserRepository userRepository;
    private final FileService fileService;
    private final FileMemoMapService fileMemoMapService;

    @Override
    @Transactional(readOnly = true)
    public List<MemoFolderDTO> getUserFolders(String userId) {
        List<MemoFolderEntity> folders = memoFolderRepository.findByUser_UserId(userId);
        return folders.stream().map(f -> {
            MemoFolderDTO dto = new MemoFolderDTO();
            dto.setFolderId(f.getFolderId());
            dto.setFolderName(f.getFolderName());
            return dto;
        }).collect(Collectors.toList());
    }

    /* 단일 메모 조회 */
    @Override
    @Transactional(readOnly = true)
    public MemoDTO getMemoById(Integer memoId) {
        MemoEntity m = memoRepository.findByIdWithFolderAndUser(memoId)
                .orElseThrow(() -> new IllegalArgumentException("해당 메모가 존재하지 않습니다. ID=" + memoId));

        MemoDTO dto = new MemoDTO();
        dto.setMemoId(m.getMemoId());
        dto.setMemoTitle(m.getMemoTitle());

        // 본문 내 이미지 경로 수정
        String contents = m.getMemoContents();
        if (contents != null) {
            contents = contents
                    .replaceAll("/static/imgs/C:/files", "http://localhost:9090/files")
                    .replaceAll("C:/files", "http://localhost:9090/files");
        }
        dto.setMemoContents(contents);

        dto.setIsFixed(m.getIsFixed());
        dto.setFolderId(m.getFolder() != null ? m.getFolder().getFolderId() : null);
        dto.setUserId(m.getUser().getUserId());
        dto.setCreateDate(m.getCreateDate());
        dto.setUpdateDate(m.getUpdateDate());

        // 첨부파일 정보 포함
        List<String> fileIds = fileMemoMapService.getFileIdsByMemoId(m.getMemoId());
        List<String> thumbnails = fileIds.stream()
                .map(id -> "http://localhost:9090/api/v1/file/" + id + "/thumbnail")
                .toList();

        dto.setFileIds(fileIds);
        dto.setFileThumbnails(thumbnails);

        return dto;
    }


    @Override
    @Transactional(readOnly = true)
    public List<MemoDTO> getMemosByFolder(String userId, Integer folderId) {
        return memoRepository.findByUserAndFolder(userId, folderId)
                .stream()
                .map(this::convertToDTO)  // 공통 변환 사용
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemoFolderWithMemosDTO> getAllFoldersWithMemos(String userId) {
        // 사용자 전체 메모 조회 (폴더 포함)
        List<MemoEntity> userMemos = memoRepository.findAllByUserIdWithFolder(userId);

        // 폴더별 그룹핑
        Map<Integer, List<MemoEntity>> grouped = userMemos.stream()
                .filter(m -> m.getFolder() != null)
                .collect(Collectors.groupingBy(m -> m.getFolder().getFolderId()));

        List<MemoFolderWithMemosDTO> result = new ArrayList<>();

        for (Map.Entry<Integer, List<MemoEntity>> entry : grouped.entrySet()) {
            MemoFolderWithMemosDTO folderDTO = new MemoFolderWithMemosDTO();
            MemoEntity firstMemo = entry.getValue().get(0);

            folderDTO.setFolderId(firstMemo.getFolder().getFolderId());
            folderDTO.setFolderName(firstMemo.getFolder().getFolderName());

            // 메모 리스트 변환
            List<MemoDTO> memoList = entry.getValue().stream().map(m -> {
                MemoDTO dto = new MemoDTO();
                dto.setMemoId(m.getMemoId());
                dto.setMemoTitle(m.getMemoTitle());

                // C:/files 경로 → API URL로 자동 변환
                String contents = m.getMemoContents();
                if (contents != null) {
                    contents = contents
                            .replaceAll("/static/imgs/C:/files/modu", "http://localhost:9090/files")
                            .replaceAll("C:/files/modu", "http://localhost:9090/files");
                }
                dto.setMemoContents(contents);

                dto.setIsFixed(m.getIsFixed());
                dto.setFolderId(m.getFolder().getFolderId());
                dto.setUserId(m.getUser().getUserId());
                dto.setCreateDate(m.getCreateDate());
                dto.setUpdateDate(m.getUpdateDate());

                // 첨부파일 ID 목록
                List<String> fileIds = fileMemoMapService.getFileIdsByMemoId(m.getMemoId());

                // 썸네일 URL 생성
                List<String> fileThumbnails = fileIds.stream()
                        .map(id -> "http://localhost:9090/api/v1/file/" + id + "/thumbnail")
                        .collect(Collectors.toList());

                dto.setFileIds(fileIds);
                dto.setFileThumbnails(fileThumbnails);

                return dto;
            }).collect(Collectors.toList());

            folderDTO.setMemos(memoList);
            result.add(folderDTO);
        }

        return result;
    }

    @Transactional
    public void toggleMemoPin(int memoId) {
        MemoEntity memo = memoRepository.findById(memoId)
                .orElseThrow(() -> new RuntimeException("메모를 찾을 수 없습니다: " + memoId));

        // "Y" ↔ "N" 토글
        memo.setIsFixed("Y".equals(memo.getIsFixed()) ? "N" : "Y");
    }

    //선택한 메모 삭제
    @Transactional
    public void deleteMemo(int memoId) {
        if (!memoRepository.existsById(memoId)) {
            throw new RuntimeException("삭제할 메모가 존재하지 않습니다: " + memoId);
        }
        memoRepository.deleteById(memoId);
    }

    //폴더 이동
    @Transactional
    public void moveMemosToFolder(MoveFolderRequest request) {
        MemoFolderEntity targetFolder = memoFolderRepository.findById(request.getTargetFolderId())
                .orElseThrow(() -> new RuntimeException("대상 폴더가 존재하지 않습니다."));

        List<MemoEntity> memos = memoRepository.findAllById(request.getMemoIds());
        if (memos.isEmpty()) throw new RuntimeException("이동할 메모가 없습니다.");

        for (MemoEntity memo : memos) {
            memo.setFolder(targetFolder); // 폴더 변경
        }
    }
    //메모 추가
    @Transactional
    public MemoEntity addMemo(MemoDTO dto) {
        UserEntity user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found: " + dto.getUserId()));

        MemoFolderEntity folder = null;
        if (dto.getFolderId() != null) {
            folder = memoFolderRepository.findById(dto.getFolderId())
                    .orElseThrow(() -> new RuntimeException("Folder not found: " + dto.getFolderId()));
        }

        MemoEntity memo = MemoEntity.builder()
                .user(user)
                .folder(folder)
                .memoTitle(dto.getMemoTitle())
                .memoContents(dto.getMemoContents())
                .isFixed(dto.getIsFixed() != null ? dto.getIsFixed() : "N")
                .build();

        MemoEntity saved = memoRepository.save(memo);

        // 첨부된 파일 매핑
        if (dto.getFileIds() != null && !dto.getFileIds().isEmpty()) {
            for (String fileId : dto.getFileIds()) {
                fileMemoMapService.linkFileToMemo(saved.getMemoId(), fileId);
            }
        }

        return saved;
    }

    /* MemoEntity → MemoDTO 변환 (썸네일 URL 포함) */
    private MemoDTO convertToDTO(MemoEntity m) {
        MemoDTO dto = new MemoDTO();
        dto.setMemoId(m.getMemoId());
        dto.setMemoTitle(m.getMemoTitle());
        dto.setMemoContents(m.getMemoContents());
        dto.setIsFixed(m.getIsFixed());
        dto.setFolderId(m.getFolder() != null ? m.getFolder().getFolderId() : null);
        dto.setUserId(m.getUser().getUserId());
        dto.setCreateDate(m.getCreateDate());
        dto.setUpdateDate(m.getUpdateDate());

        // 첨부 파일 ID와 썸네일 경로 생성
        List<String> fileIds = fileMemoMapService.getFileIdsByMemoId(m.getMemoId());
        dto.setFileIds(fileIds);
        dto.setFileThumbnails(
                fileIds.stream()
                        .map(id -> "http://localhost:9090/api/v1/file/" + id + "/thumbnail")
                        .collect(Collectors.toList())
        );
        return dto;
    }

    @Override
    @Transactional
    public MemoDTO updateMemo(Integer memoId, MemoDTO dto) {
        MemoEntity memo = memoRepository.findById(memoId)
                .orElseThrow(() -> new RuntimeException("메모를 찾을 수 없습니다: " + memoId));

        // 제목 수정
        if (dto.getMemoTitle() != null) {
            memo.setMemoTitle(dto.getMemoTitle());
        }

        // 내용 수정
        if (dto.getMemoContents() != null) {
            memo.setMemoContents(dto.getMemoContents());
        }

        // 고정 여부 (Y/N)
        if (dto.getIsFixed() != null) {
            memo.setIsFixed(dto.getIsFixed());
        }

        // 폴더 변경
        if (dto.getFolderId() != null) {
            MemoFolderEntity folder = memoFolderRepository.findById(dto.getFolderId())
                    .orElseThrow(() -> new RuntimeException("폴더를 찾을 수 없습니다: " + dto.getFolderId()));
            memo.setFolder(folder);
        }

        // 파일 매핑 업데이트 (있을 경우)
        if (dto.getFileIds() != null && !dto.getFileIds().isEmpty()) {
            // 기존 매핑 삭제
            fileMemoMapService.unlinkAllFilesFromMemo(memoId);

            // 새 매핑 등록
            for (String fileId : dto.getFileIds()) {
                fileMemoMapService.linkFileToMemo(memoId, fileId);
            }
        }

        MemoEntity saved = memoRepository.save(memo);
        return convertToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemoDTO> searchMemos(String userId, String keyword, String range) {
        String lowerKeyword = "%" + keyword.toLowerCase() + "%";

        List<MemoEntity> entities;
        switch (range) {
            case "제목":
                entities = memoRepository.findByUserIdAndTitleContainingIgnoreCase(userId, lowerKeyword);
                break;
            case "내용":
                entities = memoRepository.findByUserIdAndContentsContainingIgnoreCase(userId, lowerKeyword);
                break;
            default: // 전체
                entities = memoRepository.findByUserIdAndKeyword(userId, lowerKeyword);
        }

        return entities.stream()
                .map(MemoDTO::from)
                .toList();
    }
}
