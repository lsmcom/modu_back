package back.code.todo.service;

import back.code.todo.dto.*;
import back.code.todo.entity.SubTodoList;
import back.code.todo.entity.TodoFolder;
import back.code.todo.entity.TodoList;
import back.code.todo.repository.SubTodoListRepository;
import back.code.todo.repository.TodoCompletionStatusRepository;
import back.code.todo.repository.TodoFolderRepository;
import back.code.todo.repository.TodoListRepository;
import back.code.milestone.service.MilestoneService;
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
import java.util.Set;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.DayOfWeek;

@Service
@RequiredArgsConstructor
public class TodoListService {

    private final TodoListRepository todoListRepository;
    private final TodoFolderRepository todoFolderRepository;
    private final SubTodoListRepository subTodoListRepository;
    private final MilestoneService milestoneService;
    private final TodoCompletionStatusRepository todoCompletionStatusRepository;
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

    // 익일 자동 이월 처리 헬퍼 함수
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
    @Transactional
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

        ZoneId kstZone = ZoneId.of("Asia/Seoul"); // ZoneId 정의
        LocalDateTime now = LocalDateTime.now(kstZone);
        DayOfWeek today = now.getDayOfWeek(); // 오늘 요일

        // ====================================================================
        // 반복 Todo 및 이월 Todo 처리 로직
        // ====================================================================

        List<TodoList> newTodosToAdd = new java.util.ArrayList<>();

        // titlesDueToday 변수 정의 및 초기화 (오류 해결)
        Set<String> titlesDueToday = todos.stream()
                .filter(todo -> todo.getDueDate() != null && todo.getDueDate().toLocalDate().isEqual(now.toLocalDate()))
                .map(TodoList::getTitle)
                .collect(Collectors.toSet());

        List<TodoList> updatedTodos = todos.stream() // updatedTodos 리스트를 활용
                .peek(todo -> {
                    // --- 1. 익일 자동 이월 처리 (이전 단계 수정 로직) ---
                    if (todo.getDueDate() != null &&
                            (todo.getAutoMigrate() != null && todo.getAutoMigrate()) &&
                            !todo.getIsCompleted()
                    ) {
                        LocalDateTime migratedDate = processAutoMigrate(todo.getDueDate(), todo.getAutoMigrate(), now);
                        if (!migratedDate.equals(todo.getDueDate())) {
                            todo.setDueDate(migratedDate);
                            // Dirty Checking에 의해 저장됨
                        }
                    }

                    // --- 2. 요일별 반복 생성 처리 ---
                    if (todo.getRepeatDays() != null && !todo.getRepeatDays().isEmpty() && !todo.getIsCompleted()) {

                        Set<Integer> repeatDays = java.util.Arrays.stream(todo.getRepeatDays().split(","))
                                .map(Integer::valueOf)
                                .collect(Collectors.toSet());

                        int clientDayIndex = today.getValue() - 1;

                        boolean isRepeatDay = repeatDays.contains(clientDayIndex);
                        boolean isDueToday = todo.getDueDate() != null && todo.getDueDate().toLocalDate().isEqual(now.toLocalDate());
                        boolean alreadyCreatedToday = titlesDueToday.contains(todo.getTitle());
                        // 중복 생성 방지 로직 강화
                        if (isRepeatDay && !isDueToday && !alreadyCreatedToday) {

                            // Todo 복제 및 마감일 설정
                            TodoList newTodo = new TodoList();
                            newTodo.setUserId(userId);
                            newTodo.setFolderId(todo.getFolderId());
                            newTodo.setTitle(todo.getTitle());
                            newTodo.setTdFixed(false);
                            newTodo.setIsCompleted(false);

                            // LocalTime 호환성 오류 해결 로직
                            LocalDateTime originalDateTime = todo.getDueDate();
                            LocalTime originalTime;

                            if (originalDateTime != null) {
                                originalTime = originalDateTime.toLocalTime();
                            } else {
                                originalTime = LocalTime.of(23, 59, 0);
                            }

                            LocalDateTime newDueDate = now.with(originalTime); // now를 기준으로 originalTime을 적용
                            newTodo.setDueDate(newDueDate);

                            Optional<Integer> maxOrderIndex = todoListRepository.findMaxOrderIndexByUserId(userId);
                            int newOrderIndex = maxOrderIndex.map(index -> index + 1).orElse(0);
                            newTodo.setOrderIndex(newOrderIndex);

                            newTodo.setCreateDate(LocalDateTime.now(kstZone));
                            newTodo.setRepeatDays(todo.getRepeatDays());
                            newTodo.setAutoMigrate(todo.getAutoMigrate());

                            newTodosToAdd.add(newTodo);
                            titlesDueToday.add(newTodo.getTitle());
                        }
                    }
                })
                .collect(Collectors.toList()); // 이월 처리된 항목을 포함한 리스트

        // 새로 생성된 Todo 항목 저장 및 목록에 추가
        List<TodoList> savedNewTodos = todoListRepository.saveAll(newTodosToAdd);
        todos.addAll(savedNewTodos); // 원본 리스트(todos)에 추가하여 최종 리스트 구성

        // 3. TodoList를 TodoResponse로 변환 (SubTodo 정보 포함)
        List<TodoResponse> todoResponses = todos.stream() // 최종 리스트인 todos 사용
                .map(todo -> convertToTodoResponse(todo, folderNameMap.getOrDefault(todo.getFolderId(), "알 수 없음")))
                .collect(Collectors.toList());

        return TodoDataResponse.builder()
                .folders(folderResponses)
                .todos(todoResponses)
                .build();
    }

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

        finalDueDate = processAutoMigrate(finalDueDate, request.getAutoMigrate(), nowKst); // 익일 자동 이월 로직 즉시 실행 (생성 시점 체크)

        TodoList newTodo = new TodoList();
        newTodo.setUserId(userId);
        newTodo.setFolderId(request.getFolderId());
        newTodo.setTitle(request.getTitle());
        newTodo.setTdFixed(request.getTdFixed() != null ? request.getTdFixed() : false);
        newTodo.setIsCompleted(false);
        newTodo.setDueDate(finalDueDate);
        newTodo.setOrderIndex(newOrderIndex);
        newTodo.setCreateDate(LocalDateTime.now());
        newTodo.setRepeatDays(request.getRepeatDays());
        newTodo.setAutoMigrate(request.getAutoMigrate());

        TodoList savedTodo = todoListRepository.save(newTodo);

        return TodoResponse.fromEntity(savedTodo, folder.getName(), Collections.emptyList());
    }

    @Transactional
    public TodoResponse updateTodo(String userId, Integer todoId, TodoUpdateRequest request) {
        TodoList todo = todoListRepository.findById(todoId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Todo ID입니다."));

        if (!todo.getUserId().equals(userId)) {
            throw new SecurityException("Todo를 수정할 권한이 없습니다.");
        }

        ZoneId kstZone = ZoneId.of("Asia/Seoul");
        LocalDateTime nowKst = LocalDateTime.now(kstZone); // nowKst 변수 정의

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

        // 익일 자동 이월 로직 즉시 실행 (수정 시점 체크)
        if (newDueDate != null) {
            // 이월 로직: isCompleted가 false인 경우에만 이월 처리
            if (!todo.getIsCompleted()) { // 미완료 상태인 경우에만 이월 로직을 태웁니다.
                newDueDate = processAutoMigrate(newDueDate, request.getAutoMigrate(), nowKst);
            }
        }

        todo.setDueDate(newDueDate); // 이월 처리된 newDueDate 사용
        todo.setRepeatDays(request.getRepeatDays());
        todo.setAutoMigrate(request.getAutoMigrate());

        return convertToTodoResponse(todo, folder.getName());
    }

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

        return convertToTodoResponse(todo, folder.getName());
    }

    @Transactional
    public void deleteTodo(String userId, Integer todoId) {
        TodoList todo = todoListRepository.findById(todoId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Todo ID입니다."));

        if (!todo.getUserId().equals(userId)) {
            throw new SecurityException("Todo를 삭제할 권한이 없습니다.");
        }

        todoCompletionStatusRepository.incrementCompletionCount(userId);

        todoListRepository.delete(todo);

        /* Note: 외래 키 제약조건으로 인해 SubTodoList도 자동 삭제 예상 */
        milestoneService.checkAndAwardMilestones(userId);
    }

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

            todo.setOrderIndex(request.getOrderIndex());
        }
    }
}