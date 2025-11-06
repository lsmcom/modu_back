package back.code.todo.controller;

import back.code.todo.dto.*;
import back.code.todo.service.TodoListService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/todos")
@RequiredArgsConstructor
public class TodoListController {

    private final TodoListService todoListService;
    /**
     * 초기 로딩을 위한 모든 Todo 항목과 폴더 목록을 조회합니다.
     * GET /api/todos/data
     * @param userId 요청 헤더에서 추출된 사용자 ID
     */
    @GetMapping("/data")
    public ResponseEntity<TodoDataResponse> getInitialData(
            @RequestHeader("X-User-Id") String userId) {
        TodoDataResponse data = todoListService.getInitialData(userId);
        return ResponseEntity.ok(data);
    }

    /**
     * 새 Todo 항목 생성
     * POST /api/todos (프론트 handleAddTodo에 매핑)
     * @param userId 요청 헤더에서 추출된 사용자 ID
     */
    @PostMapping
    public ResponseEntity<TodoResponse> createTodo(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody TodoCreateRequest request) {
        TodoResponse response = todoListService.createTodo(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Todo 항목 수정 (제목, 폴더, 마감일 등 전체 업데이트)
     * PUT /api/todos/{todoId} (프론트 handleEditTodo에 매핑)
     * @param userId 요청 헤더에서 추출된 사용자 ID
     */
    @PutMapping("/{todoId}")
    public ResponseEntity<TodoResponse> updateTodo(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Integer todoId,
            @RequestBody TodoUpdateRequest request) {
        request.setTodoId(todoId);
        TodoResponse response = todoListService.updateTodo(userId, todoId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Todo 항목 고정 상태(tdFixed) 토글
     * PATCH /api/todos/{todoId}/fixed (프론트 handleToggleFixed에 매핑)
     * @param userId 요청 헤더에서 추출된 사용자 ID
     */
    @PatchMapping("/{todoId}/fixed")
    public ResponseEntity<TodoResponse> toggleFixed(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Integer todoId) {
        TodoResponse response = todoListService.toggleFixed(userId, todoId);
        return ResponseEntity.ok(response);
    }

    /**
     * Todo 항목 삭제 (프론트 handleToggleComplete에 매핑 - 완료 시 삭제)
     * DELETE /api/todos/{todoId}
     * @param userId 요청 헤더에서 추출된 사용자 ID
     */
    @DeleteMapping("/{todoId}")
    public ResponseEntity<Void> deleteTodo(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Integer todoId) {
        todoListService.deleteTodo(userId, todoId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Todo 항목 정렬 순서 일괄 업데이트
     * PATCH /api/todos/reorder (프론트 handleReorderTodos에 매핑)
     * @param userId 요청 헤더에서 추출된 사용자 ID
     */
    @PatchMapping("/reorder")
    public ResponseEntity<Void> reorderTodos(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody List<TodoReorderRequest> requests) {
        todoListService.reorderTodos(userId, requests);
        return ResponseEntity.noContent().build();
    }
}