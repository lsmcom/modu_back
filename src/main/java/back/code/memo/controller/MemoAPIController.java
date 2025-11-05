package back.code.memo.controller;

import back.code.memo.dto.MemoDTO;
import back.code.memo.dto.MemoFolderDTO;
import back.code.memo.dto.MemoFolderWithMemosDTO;
import back.code.memo.dto.MoveFolderRequest;
import back.code.memo.entity.MemoEntity;
import back.code.memo.service.MemoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/memo")
@RequiredArgsConstructor
public class MemoAPIController {

    private final MemoService memoService;

    // 로그인된 사용자의 폴더 조회
    @GetMapping("/folders/{userId}")
    public List<MemoFolderDTO> getUserFolders(@PathVariable String userId) {
        System.out.println("폴더 조회");
        return memoService.getUserFolders(userId);
    }

    // 특정 폴더의 메모 조회
    @GetMapping("/folder/{folderId}/memos")
    public List<MemoDTO> getFolderMemos(
            @PathVariable Integer folderId,
            @RequestParam String userId) {
        return memoService.getMemosByFolder(userId, folderId);
    }

    /* 단일 메모 조회 */
    @GetMapping("/{memoId}")
    public ResponseEntity<MemoDTO> getMemoById(@PathVariable Integer memoId) {
        MemoDTO memo = memoService.getMemoById(memoId);
        return ResponseEntity.ok(memo);
    }

    // 로그인된 사용자의 모든 폴더 + 메모 조회
    @GetMapping("/all/{userId}")
    public List<MemoFolderWithMemosDTO> getAllFoldersWithMemos(@PathVariable String userId) {
        return memoService.getAllFoldersWithMemos(userId);
    }

    /* 메모 고정 상태 토글 */
    @PatchMapping("/{memoId}/toggle-pin")
    public ResponseEntity<Void> toggleMemoPin(@PathVariable int memoId) {
        memoService.toggleMemoPin(memoId);
        return ResponseEntity.ok().build();
    }

    //메모 폴더 이동
    @PatchMapping("/move-folder")
    public ResponseEntity<Void> moveMemosToFolder(@RequestBody MoveFolderRequest request) {
        memoService.moveMemosToFolder(request);
        return ResponseEntity.ok().build();
    }

    // 메모 삭제
    @DeleteMapping("/{memoId}")
    public ResponseEntity<Void> deleteMemo(@PathVariable int memoId) {
        memoService.deleteMemo(memoId);
        return ResponseEntity.noContent().build();
    }

    // 메모 등록
    @PostMapping("/add")
    public MemoEntity addMemo(@RequestBody MemoDTO dto) {
        return memoService.addMemo(dto);
    }

    // 메모 수정
    @PutMapping("/{memoId}")
    public ResponseEntity<MemoDTO> updateMemo(
            @PathVariable Integer memoId,
            @RequestBody MemoDTO dto) {
        MemoDTO updated = memoService.updateMemo(memoId, dto);
        return ResponseEntity.ok(updated);
    }

    // 메모 검색
    @GetMapping("/search")
    public ResponseEntity<List<MemoDTO>> searchMemos(
            @RequestParam String userId,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "전체") String range
    ) {
        List<MemoDTO> results = memoService.searchMemos(userId, keyword, range);
        return ResponseEntity.ok(results);
    }
}
