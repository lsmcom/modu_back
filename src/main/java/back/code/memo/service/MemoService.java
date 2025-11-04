package back.code.memo.service;

import back.code.memo.dto.MemoDTO;
import back.code.memo.dto.MemoFolderDTO;
import back.code.memo.dto.MemoFolderWithMemosDTO;
import back.code.memo.dto.MoveFolderRequest;
import back.code.memo.entity.MemoEntity;

import java.util.List;

public interface MemoService {

    // 로그인된 사용자의 모든 폴더 조회
    List<MemoFolderDTO> getUserFolders(String userId);

    // 특정 폴더의 모든 메모 조회
    List<MemoDTO> getMemosByFolder(String userId, Integer folderId);
    
    //전체 데이터 조회
    List<MemoFolderWithMemosDTO> getAllFoldersWithMemos(String userId);

    //고정핀 토글
    public void toggleMemoPin(int memoId);

    //메모 삭제
    public void deleteMemo(int memoId);

    //폴더 이동
    public void moveMemosToFolder(MoveFolderRequest request);
    
    //메모 추가
    public MemoEntity addMemo(MemoDTO dto);
}
