package back.code.todo.controller;

import back.code.todo.dto.SubTodoCreateRequest;
import back.code.todo.dto.SubTodoResponse;
import back.code.todo.dto.SubTodoUpdateRequest;
import back.code.todo.service.SubTodoListService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subtodos")
@RequiredArgsConstructor
public class SubTodoListController {

    private final SubTodoListService subTodoListService;

    /**
     * 특정 상위 Todo 항목에 새로운 SubTodo 항목을 생성합니다.
     * POST /api/subtodos
     * @param userId 요청 헤더에서 추출된 사용자 ID
     */
    @PostMapping
    public ResponseEntity<SubTodoResponse> createSubTodo(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody SubTodoCreateRequest request) {
        SubTodoResponse response = subTodoListService.createSubTodo(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 특정 상위 Todo 항목의 모든 SubTodo 항목을 조회합니다.
     * GET /api/subtodos/parent/{todoId}
     * (이 API는 초기 로딩 시 TodoListController.getInitialData로 대체될 수 있으나, 개별 조회용으로 유지)
     * @param userId 요청 헤더에서 추출된 사용자 ID
     */
    @GetMapping("/parent/{todoId}")
    public ResponseEntity<List<SubTodoResponse>> getSubTodosByParentId(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable("todoId") Integer todoId) {
        List<SubTodoResponse> subTodos = subTodoListService.getSubTodosByParentId(userId, todoId);
        return ResponseEntity.ok(subTodos);
    }

    /**
     * SubTodo 항목의 내용을 수정합니다. (내용 수정만 가능)
     * PATCH /api/subtodos/{subTodoId}
     * @param userId 요청 헤더에서 추출된 사용자 ID
     */
    @PatchMapping("/{subTodoId}")
    public ResponseEntity<SubTodoResponse> updateSubTodo(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable("subTodoId") Integer subTodoId,
            @RequestBody SubTodoUpdateRequest request) {
        // DTO에 ID를 포함하여 보내는 경우도 있지만, PathVariable을 우선하여 사용합니다.
        request.setId(subTodoId);
        SubTodoResponse response = subTodoListService.updateSubTodo(userId, subTodoId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * SubTodo 항목을 삭제합니다.
     * DELETE /api/subtodos/{subTodoId}
     * @param userId 요청 헤더에서 추출된 사용자 ID
     */
    @DeleteMapping("/{subTodoId}")
    public ResponseEntity<Void> deleteSubTodo(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable("subTodoId") Integer subTodoId) {
        subTodoListService.deleteSubTodo(userId, subTodoId);
        return ResponseEntity.noContent().build();
    }
}