package back.code.todo.service;

import back.code.todo.dto.*;
import back.code.todo.entity.TodoFolder;
import back.code.todo.entity.TodoList;
import back.code.todo.repository.TodoFolderRepository;
import back.code.todo.repository.TodoListRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TodoListService {

    private final TodoListRepository todoListRepository;
    private final TodoFolderRepository todoFolderRepository;

    /**
     * 애플리케이션 초기 로드를 위한 전체 Todo와 폴더 목록을 조회합니다.
     *
     * @param userId 사용자 ID
     * @return TodoDataResponse (todos 및 folders 포함)
     */
    public TodoDataResponse getInitialData(String userId) {
        // 1. 모든 TodoFolder 조회
        List<TodoFolder> folders = todoFolderRepository.findByUserId(userId);
        Map<Integer, String> folderNameMap = folders.stream()
                .collect(Collectors.toMap(TodoFolder::getFolderId, TodoFolder::getName));

        List<FolderResponse> folderResponses = folders.stream()
                .map(FolderResponse::fromEntity)
                .collect(Collectors.toList());

        // 2. 모든 TodoList 조회 (order_index 순으로 정렬되어 조회)
        List<TodoList> todos = todoListRepository.findByUserIdOrderByOrderIndexAsc(userId);

        List<TodoResponse> todoResponses = todos.stream()
                .map(todo -> TodoResponse.fromEntity(todo, folderNameMap.getOrDefault(todo.getFolderId(), "알 수 없음")))
                .collect(Collectors.toList());

        return TodoDataResponse.builder()
                .folders(folderResponses)
                .todos(todoResponses)
                .build();
    }

    /**
     * 새로운 할 일 항목을 생성하고 저장합니다.
     *
     * @param userId 사용자 ID
     * @param request Todo 생성 요청 DTO
     * @return 생성된 Todo 항목의 Response DTO
     */
    @Transactional
    public TodoResponse createTodo(String userId, TodoCreateRequest request) {
        // 1. 폴더 존재 여부 확인
        TodoFolder folder = todoFolderRepository.findById(request.getFolderId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 폴더 ID입니다."));

        Optional<Integer> maxOrderIndex = todoListRepository.findMaxOrderIndexByUserId(userId);
        int newOrderIndex = maxOrderIndex.map(index -> index + 1).orElse(0);

        TodoList newTodo = new TodoList();
        newTodo.setUserId(userId);
        newTodo.setFolderId(request.getFolderId());
        newTodo.setTitle(request.getTitle());
        newTodo.setSubTitle(request.getSubTitle());
        newTodo.setTdFixed(request.getTdFixed() != null ? request.getTdFixed() : false);
        newTodo.setIsCompleted(false); // 새로 생성되는 항목은 항상 미완료
        newTodo.setDueDate(request.getDueDate());
        newTodo.setOrderIndex(newOrderIndex); // 새 항목을 목록 끝에 추가
        newTodo.setCreateDate(Instant.now());
        newTodo.setRepeatDays(request.getRepeatDays());
        newTodo.setAutoMigrate(request.getAutoMigrate());

        TodoList savedTodo = todoListRepository.save(newTodo);

        return TodoResponse.fromEntity(savedTodo, folder.getName());
    }

    /**
     * 기존 할 일 항목을 수정합니다.
     *
     * @param userId 사용자 ID (권한 확인용)
     * @param todoId 수정할 할 일 ID
     * @param request Todo 수정 요청 DTO
     * @return 수정된 Todo 항목의 Response DTO
     */
    @Transactional
    public TodoResponse updateTodo(String userId, Integer todoId, TodoUpdateRequest request) {
        TodoList todo = todoListRepository.findById(todoId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Todo ID입니다."));

        if (!todo.getUserId().equals(userId)) {
            throw new SecurityException("Todo를 수정할 권한이 없습니다.");
        }

        // 1. 폴더 존재 여부 확인 및 폴더 이름 조회
        TodoFolder folder = todoFolderRepository.findById(request.getFolderId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 폴더 ID입니다."));

        // 2. 필드 업데이트
        todo.setTitle(request.getTitle());
        todo.setFolderId(request.getFolderId());
        todo.setSubTitle(request.getSubTitle());

        // isCompleted 필드는 요청에 따라 토글 가능
        if (request.getIsCompleted() != null) {
            todo.setIsCompleted(request.getIsCompleted());
        }

        // tdFixed 필드는 요청에 따라 토글 가능
        if (request.getTdFixed() != null) {
            todo.setTdFixed(request.getTdFixed());
        }

        todo.setDueDate(request.getDueDate());
        todo.setRepeatDays(request.getRepeatDays());
        todo.setAutoMigrate(request.getAutoMigrate());

        // save() 호출 없이 @Transactional에 의해 자동 업데이트
        return TodoResponse.fromEntity(todo, folder.getName());
    }

    /**
     * Todo 항목의 고정 상태(tdFixed)를 토글합니다.
     * (프론트엔드 handleToggleFixed에 매핑)
     *
     * @param userId 사용자 ID
     * @param todoId 고정 상태를 변경할 할 일 ID
     * @return 변경된 Todo 항목의 Response DTO
     */
    @Transactional
    public TodoResponse toggleFixed(String userId, Integer todoId) {
        TodoList todo = todoListRepository.findById(todoId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Todo ID입니다."));

        if (!todo.getUserId().equals(userId)) {
            throw new SecurityException("Todo를 수정할 권한이 없습니다.");
        }

        todo.setTdFixed(!todo.getTdFixed());

        // 폴더 이름 조회를 위한 폴더 엔티티
        TodoFolder folder = todoFolderRepository.findById(todo.getFolderId())
                .orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다."));

        return TodoResponse.fromEntity(todo, folder.getName());
    }

    /**
     * Todo 항목을 삭제합니다.
     * (프론트엔드 handleToggleComplete에 매핑 - 프론트에서 완료 시 DB에서 바로 삭제하는 로직)
     *
     * @param userId 사용자 ID
     * @param todoId 삭제할 할 일 ID
     */
    @Transactional
    public void deleteTodo(String userId, Integer todoId) {
        TodoList todo = todoListRepository.findById(todoId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Todo ID입니다."));

        if (!todo.getUserId().equals(userId)) {
            throw new SecurityException("Todo를 삭제할 권한이 없습니다.");
        }

        todoListRepository.delete(todo);
    }

    /**
     * Todo 항목의 순서를 업데이트합니다.
     * (프론트엔드 handleReorderTodos에 매핑)
     *
     * @param userId 사용자 ID
     * @param requests 순서가 변경된 Todo 목록 DTO
     */
    @Transactional
    public void reorderTodos(String userId, List<TodoReorderRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return;
        }

        for (TodoReorderRequest request : requests) {
            TodoList todo = todoListRepository.findById(request.getTodoId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Todo ID입니다: " + request.getTodoId()));

            if (!todo.getUserId().equals(userId)) {
                throw new SecurityException("Todo를 수정할 권한이 없습니다: " + request.getTodoId());
            }

            // 요청된 새로운 순서 인덱스로 업데이트
            todo.setOrderIndex(request.getOrderIndex());
        }
        // JpaRepository.saveAll(iterable)을 사용하여 일괄 업데이트도 가능하지만,
        // @Transactional 내부에서 엔티티를 로드하여 변경하면 자동 Dirty Checking을 통해 업데이트됩니다.
    }

    /**
     * SubTodo (부제/상세 내용)를 삭제합니다.
     * (프론트엔드 handleDeleteSubTodo에 매핑)
     *
     * @param userId 사용자 ID
     * @param todoId SubTitle을 삭제할 할 일 ID
     * @return 변경된 Todo 항목의 Response DTO
     */
    @Transactional
    public TodoResponse deleteSubTodo(String userId, Integer todoId) {
        TodoList todo = todoListRepository.findById(todoId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Todo ID입니다."));

        if (!todo.getUserId().equals(userId)) {
            throw new SecurityException("Todo를 수정할 권한이 없습니다.");
        }

        todo.setSubTitle(null);

        // 폴더 이름 조회를 위한 폴더 엔티티
        TodoFolder folder = todoFolderRepository.findById(todo.getFolderId())
                .orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다."));

        return TodoResponse.fromEntity(todo, folder.getName());
    }
}