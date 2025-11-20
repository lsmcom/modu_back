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

    @GetMapping("/data")
    public ResponseEntity<TodoDataResponse> getInitialData(
            @RequestHeader("X-User-Id") String userId) {
        TodoDataResponse data = todoListService.getInitialData(userId);
        return ResponseEntity.ok(data);
    }

    @PostMapping
    public ResponseEntity<TodoResponse> createTodo(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody TodoCreateRequest request) {
        TodoResponse response = todoListService.createTodo(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{todoId}")
    public ResponseEntity<TodoResponse> updateTodo(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable("todoId") Integer todoId,
            @RequestBody TodoUpdateRequest request) {
        request.setTodoId(todoId);
        TodoResponse response = todoListService.updateTodo(userId, todoId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{todoId}/fixed")
    public ResponseEntity<TodoResponse> toggleFixed(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable("todoId") Integer todoId) {
        TodoResponse response = todoListService.toggleFixed(userId, todoId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{todoId}")
    public ResponseEntity<Void> deleteTodo(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable("todoId") Integer todoId) {
        todoListService.deleteTodo(userId, todoId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/reorder")
    public ResponseEntity<Void> reorderTodos(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody List<TodoReorderRequest> requests) {
        todoListService.reorderTodos(userId, requests);
        return ResponseEntity.noContent().build();
    }
}