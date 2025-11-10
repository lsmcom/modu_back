package back.code.todo.service;

import back.code.todo.dto.*;
import back.code.todo.entity.SubTodoList;
import back.code.todo.entity.TodoFolder;
import back.code.todo.entity.TodoList;
import back.code.todo.repository.SubTodoListRepository;
import back.code.todo.repository.TodoFolderRepository;
import back.code.todo.repository.TodoListRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class TodoListService {

    private final TodoListRepository todoListRepository;
    private final TodoFolderRepository todoFolderRepository;
    private final SubTodoListRepository subTodoListRepository;

    /**
     * TodoList를 SubTodoResponse를 포함한 TodoResponse로 변환하는 헬퍼 메소드
     */
    private TodoResponse convertToTodoResponse(TodoList todo, String folderName) {
        // 해당 TodoId에 연결된 모든 SubTodoList 항목 조회
        List<SubTodoList> subTodos = subTodoListRepository.findByTodoListId(todo.getTodoId());

        // SubTodoList를 SubTodoResponse DTO로 변환
        List<SubTodoResponse> subTodoResponses = subTodos.stream()
                .map(SubTodoResponse::fromEntity)
                .collect(Collectors.toList());

        // SubTodoResponse 리스트를 포함하여 TodoResponse 생성 (새로운 fromEntity 오버로드 사용)
        return TodoResponse.fromEntity(todo, folderName, subTodoResponses);
    }

    // 💡 [추가] 익일 자동 이월 처리 헬퍼 함수
    private LocalDateTime processAutoMigrate(LocalDateTime dueDate, Boolean autoMigrate, LocalDateTime now) {
        if (dueDate != null && autoMigrate != null && autoMigrate) {

            // dueDate를 날짜만 비교하기 위해 시간 부분을 0으로 설정
            LocalDateTime dueDateOnly = dueDate.with(LocalTime.MIN);
            LocalDateTime nowOnly = now.with(LocalTime.MIN);

            // 마감일이 현재 날짜보다 과거이고, 완료되지 않은 항목일 경우 이월
            if (dueDateOnly.isBefore(nowOnly)) {
                // 이월 로직: 마감일을 현재 날짜의 다음 날로 변경
                return dueDateOnly.plusDays(1).with(dueDate.toLocalTime());
            }
        }
        return dueDate;
    }

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

        // 💡 [추가] 초기 데이터 로드 시에도 이월이 필요한 항목을 즉시 이월 처리합니다.
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

        List<TodoList> updatedTodos = todos.stream()
                .peek(todo -> {
                    if (todo.getDueDate() != null &&
                            (todo.getAutoMigrate() != null && todo.getAutoMigrate()) &&
                            !todo.getIsCompleted() // 미완료된 항목만 이월
                    ) {
                        LocalDateTime migratedDate = processAutoMigrate(todo.getDueDate(), todo.getAutoMigrate(), now);
                        if (!migratedDate.equals(todo.getDueDate())) {
                            todo.setDueDate(migratedDate);
                            // 💡 [추가] DB에 즉시 반영 (Dirty Checking)
                            // todoListRepository.save(todo); // @Transactional이므로 명시적 save는 선택 사항이지만, 안전을 위해 호출 가능
                        }
                    }
                })
                .collect(Collectors.toList());

        // 3. TodoList를 TodoResponse로 변환 (SubTodo 정보 포함)
        List<TodoResponse> todoResponses = updatedTodos.stream()
                .map(todo -> convertToTodoResponse(todo, folderNameMap.getOrDefault(todo.getFolderId(), "알 수 없음")))
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

        ZoneId kstZone = ZoneId.of("Asia/Seoul");
        LocalDateTime nowKst = LocalDateTime.now(kstZone);

        LocalDateTime finalDueDate = request.getDueDate();
        if (finalDueDate == null) {
            finalDueDate = nowKst.with(LocalTime.of(23, 59, 0));
        }

        // 💡 [추가] 익일 자동 이월 로직 즉시 실행 (생성 시점 체크)
        finalDueDate = processAutoMigrate(finalDueDate, request.getAutoMigrate(), nowKst);

        TodoList newTodo = new TodoList();
        newTodo.setUserId(userId);
        newTodo.setFolderId(request.getFolderId());
        newTodo.setTitle(request.getTitle());
        newTodo.setTdFixed(request.getTdFixed() != null ? request.getTdFixed() : false);
        newTodo.setIsCompleted(false); // 새로 생성되는 항목은 항상 미완료
        newTodo.setDueDate(finalDueDate);
        newTodo.setOrderIndex(newOrderIndex); // 새 항목을 목록 끝에 추가
        newTodo.setCreateDate(LocalDateTime.now());
        newTodo.setRepeatDays(request.getRepeatDays());
        newTodo.setAutoMigrate(request.getAutoMigrate());

        TodoList savedTodo = todoListRepository.save(newTodo);

        // 생성 시점에는 하위 할 일이 없으므로 빈 리스트 반환
        return TodoResponse.fromEntity(savedTodo, folder.getName(), Collections.emptyList());
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

        // 💡 [추가] nowKst 변수 정의
        ZoneId kstZone = ZoneId.of("Asia/Seoul");
        LocalDateTime nowKst = LocalDateTime.now(kstZone);

        // 1. 폴더 존재 여부 확인 및 폴더 이름 조회
        TodoFolder folder = todoFolderRepository.findById(request.getFolderId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 폴더 ID입니다."));

        // 2. 필드 업데이트
        todo.setTitle(request.getTitle());
        todo.setFolderId(request.getFolderId());


        // isCompleted 필드는 요청에 따라 토글 가능
        if (request.getIsCompleted() != null) {
            todo.setIsCompleted(request.getIsCompleted());
        }

        // tdFixed 필드는 요청에 따라 토글 가능
        if (request.getTdFixed() != null) {
            todo.setTdFixed(request.getTdFixed());
        }

        LocalDateTime newDueDate = request.getDueDate();

        // 💡 [추가] 익일 자동 이월 로직 즉시 실행 (수정 시점 체크)
        if (newDueDate != null) {
            // 이월 로직: isCompleted가 false인 경우에만 이월 처리
            if (!todo.getIsCompleted()) { // 💡 [추가] 미완료 상태인 경우에만 이월 로직을 태웁니다.
                newDueDate = processAutoMigrate(newDueDate, request.getAutoMigrate(), nowKst);
            }
        }

        todo.setDueDate(newDueDate); // 💡 [수정] 이월 처리된 newDueDate 사용
        todo.setRepeatDays(request.getRepeatDays());
        todo.setAutoMigrate(request.getAutoMigrate());

        // save() 호출 없이 @Transactional에 의해 자동 업데이트
        return convertToTodoResponse(todo, folder.getName()); // 수정된 부분: 헬퍼 메소드 사용
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

        return convertToTodoResponse(todo, folder.getName()); // 수정된 부분: 헬퍼 메소드 사용
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

        /*
         * Note: DB 스키마에 fk_subtodo_todo 외래 키에 ON DELETE CASCADE가 설정되어 있으므로,
         * TodoList 삭제 시 SubTodoList도 자동으로 삭제될 것으로 예상하고 명시적 삭제 로직은 추가하지 않습니다.
         */
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
}