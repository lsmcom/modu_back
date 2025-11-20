package back.code.memo.controller;

import back.code.memo.dto.MemoFolderDTO;
import back.code.memo.enums.FolderType;
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

    // 특정 유저의 폴더 조회
    @GetMapping("/{userId}")
    public List<MemoFolderDTO> getUserFolders(@PathVariable("userId") String userId) {
        System.out.println("폴더 조회 요청 userId=" + userId);
        return memoFolderService.getUserFolders(userId);
    }

    // 폴더 추가
    @PostMapping("/add")
    public MemoFolderDTO addFolder(@RequestBody MemoFolderDTO dto) {
        return memoFolderService.addFolder(dto);
    }

    // 폴더 수정
    @PatchMapping("/{folderId}")
    public ResponseEntity<MemoFolderDTO> updateFolder(
            @PathVariable("folderId") Integer folderId,
            @RequestBody MemoFolderDTO dto) {
        MemoFolderDTO updated = memoFolderService.updateFolder(folderId, dto);
        return ResponseEntity.ok(updated);
    }

    // 폴더 삭제
    @DeleteMapping("/{folderId}")
    public ResponseEntity<Void> deleteFolder(@PathVariable("folderId") Integer folderId) {
        memoFolderService.deleteFolder(folderId);
        return ResponseEntity.noContent().build();
    }
}
