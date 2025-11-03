package back.code.memo.service;

import back.code.memo.dto.MemoDTO;
import back.code.memo.dto.MemoFolderDTO;
import back.code.memo.dto.MemoFolderWithMemosDTO;
import back.code.memo.dto.MoveFolderRequest;
import back.code.memo.entity.MemoEntity;
import back.code.memo.entity.MemoFolderEntity;
import back.code.memo.repository.MemoRepository;
import back.code.memo.repository.MemoFolderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemoServiceImpl implements MemoService {

    private final MemoRepository memoRepository;
    private final MemoFolderRepository memoFolderRepository;

    @Transactional
    @Override
    public List<MemoFolderDTO> getUserFolders(String userId) {
        // 현재 구조상 memo_folder에는 user_id 컬럼이 없으므로
        // 사용자의 메모에 연결된 폴더를 DISTINCT로 추출
        List<MemoEntity> userMemos = memoRepository.findAll()
                .stream()
                .filter(m -> m.getUser().getUserId().equals(userId))
                .collect(Collectors.toList());

        return userMemos.stream()
                .map(MemoEntity::getFolder)
                .distinct()
                .map(f -> {
                    MemoFolderDTO dto = new MemoFolderDTO();
                    dto.setFolderId(f.getFolderId());
                    dto.setFolderName(f.getFolderName());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public List<MemoDTO> getMemosByFolder(String userId, Integer folderId) {
        return memoRepository.findByUserAndFolder(userId, folderId)
                .stream()
                .map(m -> {
                    MemoDTO dto = new MemoDTO();
                    dto.setMemoId(m.getMemoId());
                    dto.setMemoTitle(m.getMemoTitle());
                    dto.setMemoContents(m.getMemoContents());
                    dto.setIsFixed(m.getIsFixed());
                    dto.setFolderId(m.getFolder().getFolderId());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemoFolderWithMemosDTO> getAllFoldersWithMemos(String userId) {

        // 사용자 전체 메모 조회 (폴더 포함)
        List<MemoEntity> userMemos = memoRepository.findAllByUserIdWithFolder(userId);

        // 폴더별로 그룹핑
        Map<Integer, List<MemoEntity>> grouped = userMemos.stream()
                .filter(m -> m.getFolder() != null)
                .collect(Collectors.groupingBy(m -> m.getFolder().getFolderId()));

        // 폴더 + 메모 DTO로 변환
        List<MemoFolderWithMemosDTO> result = new ArrayList<>();

        for (Map.Entry<Integer, List<MemoEntity>> entry : grouped.entrySet()) {
            MemoFolderWithMemosDTO folderDTO = new MemoFolderWithMemosDTO();
            MemoEntity firstMemo = entry.getValue().get(0);

            folderDTO.setFolderId(firstMemo.getFolder().getFolderId());
            folderDTO.setFolderName(firstMemo.getFolder().getFolderName());

            List<MemoDTO> memoList = entry.getValue().stream().map(m -> {
                MemoDTO dto = new MemoDTO();
                dto.setMemoId(m.getMemoId());
                dto.setMemoTitle(m.getMemoTitle());
                dto.setMemoContents(m.getMemoContents());
                dto.setIsFixed(m.getIsFixed());
                dto.setFolderId(m.getFolder().getFolderId());
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
}
