package back.code.todo.service;

import back.code.todo.dto.FolderCreateRequest;
import back.code.todo.dto.FolderDeleteRequest;
import back.code.todo.dto.FolderResponse;
import back.code.todo.dto.FolderUpdateRequest;
import back.code.todo.entity.TodoFolder;
import back.code.todo.repository.TodoFolderRepository;
import back.code.todo.repository.TodoListRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TodoFolderService {

    private final TodoFolderRepository todoFolderRepository;
    private final TodoListRepository todoListRepository;

    // 프론트엔드에서 고정된 ID를 사용하는 '전체' 및 'NotTodo'를 정의
    private static final Set<Integer> NON_DELETABLE_FOLDER_IDS = Set.of(0, 1, 999);
    private static final int DEFAULT_FOLDER_ID = 1; // 기본 폴더 ID (Mock 데이터 기반 가정)

    /**
     * 특정 사용자의 모든 폴더 목록을 조회합니다.
     * @param userId 사용자 ID
     * @return FolderResponse DTO 목록
     */
    public List<FolderResponse> getAllFolders(String userId) {
        // TodoFolderRepository.findByUserId를 사용하여 사용자 폴더만 조회합니다.
        List<TodoFolder> folders = todoFolderRepository.findByUserId(userId);

        // 프론트엔드에서 '전체' 폴더(ID: 0)를 Mock으로 관리할 수도 있으나,
        // 여기서는 DB에 있는 폴더만 응답합니다. (단, NOT_TODO_FOLDER_ID 999는 DB에 있을 수 있음)
        return folders.stream()
                .map(FolderResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 새 폴더를 생성합니다.
     * @param userId 사용자 ID
     * @param request 폴더 생성 요청 DTO
     * @return 생성된 폴더의 FolderResponse DTO
     */
    public FolderResponse createFolder(String userId, FolderCreateRequest request) {
        TodoFolder newFolder = new TodoFolder();
        newFolder.setUserId(userId);
        newFolder.setName(request.getName());

        TodoFolder savedFolder = todoFolderRepository.save(newFolder);
        return FolderResponse.fromEntity(savedFolder);
    }

    /**
     * 폴더 이름을 수정합니다.
     * @param userId 사용자 ID (권한 확인용)
     * @param request 폴더 수정 요청 DTO
     * @return 수정된 폴더의 FolderResponse DTO
     */
    @Transactional
    public FolderResponse updateFolder(String userId, FolderUpdateRequest request) {
        TodoFolder folder = todoFolderRepository.findById(request.getFolderId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 폴더 ID입니다: " + request.getFolderId()));

        // 권한 검사: 요청 사용자와 폴더 소유자가 일치하는지 확인
        if (!folder.getUserId().equals(userId)) {
            throw new SecurityException("폴더를 수정할 권한이 없습니다.");
        }

        // 필수 폴더는 수정 금지 (ID 0, 1, 999)
        if (NON_DELETABLE_FOLDER_IDS.contains(folder.getFolderId())) {
            throw new IllegalArgumentException("기본 폴더는 이름을 수정할 수 없습니다.");
        }

        folder.setName(request.getName());
        // save() 호출 없이 @Transactional에 의해 자동 업데이트

        return FolderResponse.fromEntity(folder);
    }

    /**
     * 폴더를 일괄 삭제하고, 해당 폴더의 모든 Todo 항목도 삭제합니다.
     * @param userId 사용자 ID (권한 확인용)
     * @param request 삭제할 폴더 ID 목록 DTO
     */
    @Transactional
    public void deleteFolders(String userId, FolderDeleteRequest request) {
        if (request.getFolderIds() == null || request.getFolderIds().isEmpty()) {
            return;
        }

        request.getFolderIds().forEach(folderId -> {
            // 필수 폴더는 삭제 금지 (ID 0, 1, 999)
            if (NON_DELETABLE_FOLDER_IDS.contains(folderId)) {
                throw new IllegalArgumentException("기본 폴더(ID: " + folderId + ")는 삭제할 수 없습니다.");
            }

            TodoFolder folder = todoFolderRepository.findById(folderId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 폴더 ID입니다: " + folderId));

            // 권한 검사
            if (!folder.getUserId().equals(userId)) {
                throw new SecurityException("폴더를 삭제할 권한이 없습니다.");
            }

            // 폴더 삭제 (외래 키 제약조건으로 인해 해당 폴더의 Todo 항목도 자동 삭제)
            todoFolderRepository.delete(folder);
        });
    }
}