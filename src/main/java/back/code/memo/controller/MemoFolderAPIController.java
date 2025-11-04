package back.code.memo.controller;

import back.code.memo.dto.MemoFolderDTO;
import back.code.memo.service.MemoFolderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/memo/folder")
@RequiredArgsConstructor
public class MemoFolderAPIController {

    private final MemoFolderService memoFolderService;

    // 🔹 모든 폴더 조회 (전역)
    @GetMapping("/all")
    public List<MemoFolderDTO> getAllFolders() {
        return memoFolderService.getAllFolders();
    }

    // 🔹 폴더 추가
    @PostMapping("/add")
    public ResponseEntity<MemoFolderDTO> addFolder(@RequestBody MemoFolderDTO dto) {
        MemoFolderDTO saved = memoFolderService.addFolder(dto);
        return ResponseEntity.ok(saved);
    }

    // 🔹 폴더 수정
    @PatchMapping("/{folderId}")
    public ResponseEntity<MemoFolderDTO> updateFolder(
            @PathVariable Integer folderId,
            @RequestBody MemoFolderDTO dto) {
        MemoFolderDTO updated = memoFolderService.updateFolder(folderId, dto);
        return ResponseEntity.ok(updated);
    }

    // 🔹 폴더 삭제
    @DeleteMapping("/{folderId}")
    public ResponseEntity<Void> deleteFolder(@PathVariable Integer folderId) {
        memoFolderService.deleteFolder(folderId);
        return ResponseEntity.noContent().build();
    }
}