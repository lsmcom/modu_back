package back.code.todo.controller;

import back.code.todo.dto.FolderCreateRequest;
import back.code.todo.dto.FolderDeleteRequest;
import back.code.todo.dto.FolderResponse;
import back.code.todo.dto.FolderUpdateRequest;
import back.code.todo.service.TodoFolderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/folders")
@RequiredArgsConstructor
public class TodoFolderController {

    private final TodoFolderService todoFolderService;
    /**
     * 모든 폴더 목록 조회 (개별 API)
     * GET /api/folders
     * @param userId 요청 헤더에서 추출된 사용자 ID (예: "user01")
     */
    @GetMapping
    public ResponseEntity<List<FolderResponse>> getAllFolders(
            @RequestHeader("X-User-Id") String userId) {
        List<FolderResponse> folders = todoFolderService.getAllFolders(userId);
        return ResponseEntity.ok(folders);
    }

    /**
     * 새 폴더 생성
     * POST /api/folders
     * @param userId 요청 헤더에서 추출된 사용자 ID
     */
    @PostMapping
    public ResponseEntity<FolderResponse> createFolder(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody FolderCreateRequest request) {
        FolderResponse response = todoFolderService.createFolder(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 폴더 이름 수정
     * PUT /api/folders (DTO에 folderId 포함)
     * @param userId 요청 헤더에서 추출된 사용자 ID
     */
    @PutMapping
    public ResponseEntity<FolderResponse> updateFolder(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody FolderUpdateRequest request) {
        FolderResponse response = todoFolderService.updateFolder(userId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 폴더 일괄 삭제 (프론트엔드 handleDeleteFolders에 매핑)
     * DELETE /api/folders
     * @param userId 요청 헤더에서 추출된 사용자 ID
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteFolders(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody FolderDeleteRequest request) {
        todoFolderService.deleteFolders(userId, request);
        return ResponseEntity.noContent().build();
    }

}