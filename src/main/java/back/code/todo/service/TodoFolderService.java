package back.code.todo.service;

import back.code.todo.dto.FolderCreateRequest;
import back.code.todo.dto.FolderDeleteRequest;
import back.code.todo.dto.FolderResponse;
import back.code.todo.dto.FolderUpdateRequest;
import back.code.todo.entity.TodoFolder;
import back.code.todo.entity.TodoFolderId;
import back.code.todo.repository.TodoFolderRepository;
import back.code.todo.repository.TodoListRepository;
import back.code.user.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Optional;
import back.code.user.entity.UserEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;


@Service
@RequiredArgsConstructor
public class TodoFolderService {

    private final TodoFolderRepository todoFolderRepository;
    private final TodoListRepository todoListRepository;

    // 프론트엔드에서 고정된 ID를 사용하는 '기본' 및 'NotTodo'를 정의
    private static final Set<Integer> NON_DELETABLE_FOLDER_IDS = Set.of(1, 999);
    private static final int DEFAULT_FOLDER_ID = 1; // 기본 폴더 ID (Mock 데이터 기반 가정)

    // 회원가입 시 기본 폴더 (1) 및 NotTodoList 폴더 (999) 생성
    public void createDefaultTodoFolders(UserEntity user) {
        String userId = user.getUserId();

        if (!todoFolderRepository.existsByUserIdAndFolderId(userId, 1)) {
            TodoFolder defaultFolder = new TodoFolder(1, userId, "기본 폴더", true);
            todoFolderRepository.save(defaultFolder);
        }

        if (!todoFolderRepository.existsByUserIdAndFolderId(userId, 999)) {
            TodoFolder notTodoFolder = new TodoFolder(999, userId, "NotTodoList", false);
            todoFolderRepository.save(notTodoFolder);
        }
    }

    @Transactional
    public void createDefaultTodoFoldersInNewTx(UserEntity user) {
        createDefaultTodoFolders(user);
    }
    /* 특정 사용자의 모든 폴더 목록을 조회 */
    public List<FolderResponse> getAllFolders(String userId) {
        // TodoFolderRepository.findByUserId를 사용하여 사용자 폴더만 조회합니다.
        List<TodoFolder> folders = todoFolderRepository.findByUserId(userId);

        return folders.stream()
                .map(FolderResponse::fromEntity)
                .collect(Collectors.toList());
    }
    /* 새 폴더를 생성 */
    public FolderResponse createFolder(String userId, FolderCreateRequest request) {

        Optional<Integer> maxFolderId = todoFolderRepository.findMaxFolderIdByUserId(userId);
        int newFolderId = maxFolderId.map(id -> id + 1).orElse(2);
        while (NON_DELETABLE_FOLDER_IDS.contains(newFolderId)) {
            newFolderId++;
        }
        TodoFolder newFolder = new TodoFolder();
        newFolder.setUserId(userId);
        newFolder.setName(request.getName());
        newFolder.setIsDefault(false);
        newFolder.setFolderId(newFolderId);

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
        TodoFolder folder = todoFolderRepository.findById(new TodoFolderId(request.getFolderId(), userId))
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 폴더 ID이거나 접근 권한이 없습니다: " + request.getFolderId()));

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

            TodoFolder folder = todoFolderRepository.findById(new TodoFolderId(folderId, userId))
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 폴더 ID이거나 접근 권한이 없습니다: " + folderId));

            // 권한 검사
            if (!folder.getUserId().equals(userId)) {
                throw new SecurityException("폴더를 삭제할 권한이 없습니다.");
            }

            // 폴더 삭제 (외래 키 제약조건으로 인해 해당 폴더의 Todo 항목도 자동 삭제)
            todoFolderRepository.delete(folder);
        });
    }
}