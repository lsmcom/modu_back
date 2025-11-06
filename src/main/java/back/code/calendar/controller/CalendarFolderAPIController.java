package back.code.calendar.controller;

import back.code.calendar.entity.CalendarFolderEntity;
import back.code.calendar.service.CalendarFolderService;
import back.code.user.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/calendar/folder")
@RequiredArgsConstructor
public class CalendarFolderAPIController {

    private final CalendarFolderService folderService;

    /** 폴더 생성 */
    @PostMapping
    public ResponseEntity<CalendarFolderEntity> createFolder(@RequestBody CalendarFolderEntity folder) {
        return ResponseEntity.ok(folderService.createFolder(folder));
    }

    /** 사용자 폴더 목록 조회 */
    @GetMapping("/{userId}")
    public ResponseEntity<List<CalendarFolderEntity>> getFolders(@PathVariable String userId) {
        UserEntity user = new UserEntity();
        user.setUserId(userId);
        return ResponseEntity.ok(folderService.getFoldersByUser(user));
    }

    /** 폴더명 수정 */
    @PatchMapping("/{folderId}")
    public ResponseEntity<CalendarFolderEntity> updateFolder(
            @PathVariable String folderId, @RequestParam String newName) {
        return ResponseEntity.ok(folderService.updateFolder(folderId, newName));
    }

    /** 폴더 삭제 */
    @DeleteMapping("/{folderId}")
    public ResponseEntity<Void> deleteFolder(@PathVariable String folderId) {
        folderService.deleteFolder(folderId);
        return ResponseEntity.noContent().build();
    }
}

