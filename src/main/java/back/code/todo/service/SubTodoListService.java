package back.code.todo.service;

import back.code.todo.dto.SubTodoCreateRequest;
import back.code.todo.dto.SubTodoResponse;
import back.code.todo.dto.SubTodoUpdateRequest;
import back.code.todo.entity.SubTodoList;
import back.code.todo.entity.TodoList;
import back.code.todo.repository.SubTodoListRepository;
import back.code.todo.repository.TodoListRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubTodoListService {

    private final SubTodoListRepository subTodoListRepository;
    private final TodoListRepository todoListRepository; // 상위 Todo 존재 여부 확인용

    /**
     * 특정 상위 Todo 항목에 새로운 SubTodo 항목을 생성합니다.
     *
     * @param userId 사용자 ID (권한 확인용)
     * @param request SubTodo 생성 요청 DTO
     * @return 생성된 SubTodo 항목의 Response DTO
     */
    @Transactional
    public SubTodoResponse createSubTodo(String userId, SubTodoCreateRequest request) {
        // 1. 상위 Todo 존재 여부 및 권한 확인
        TodoList parentTodo = todoListRepository.findById(request.getTodoListId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상위 Todo ID입니다: " + request.getTodoListId()));

        if (!parentTodo.getUserId().equals(userId)) {
            throw new SecurityException("SubTodo를 생성할 권한이 없습니다.");
        }

        // 2. SubTodo 엔티티 생성 및 저장
        SubTodoList newSubTodo = new SubTodoList();
        newSubTodo.setTodoListId(request.getTodoListId());
        newSubTodo.setTitle(request.getTitle());

        SubTodoList savedSubTodo = subTodoListRepository.save(newSubTodo);

        return SubTodoResponse.fromEntity(savedSubTodo);
    }

    /**
     * 특정 상위 Todo 항목의 모든 SubTodo 항목을 조회합니다.
     * (TodoListService의 getInitialData에서 이미 처리되지만, 필요할 수 있음)
     *
     * @param userId 사용자 ID (권한 확인용)
     * @param todoListId 상위 Todo 항목 ID
     * @return SubTodo 항목 목록
     */
    public List<SubTodoResponse> getSubTodosByParentId(String userId, Integer todoListId) {
        // 1. 상위 Todo 존재 여부 및 권한 확인
        TodoList parentTodo = todoListRepository.findById(todoListId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상위 Todo ID입니다: " + todoListId));

        if (!parentTodo.getUserId().equals(userId)) {
            throw new SecurityException("SubTodo를 조회할 권한이 없습니다.");
        }

        // 2. SubTodo 목록 조회 및 DTO 변환
        return subTodoListRepository.findByTodoListId(todoListId).stream()
                .map(SubTodoResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * SubTodo 항목의 내용을 수정합니다.
     *
     * @param userId 사용자 ID (권한 확인용)
     * @param subTodoId SubTodo 항목 고유 ID
     * @param request SubTodo 수정 요청 DTO
     * @return 수정된 SubTodo 항목의 Response DTO
     */
    @Transactional
    public SubTodoResponse updateSubTodo(String userId, Integer subTodoId, SubTodoUpdateRequest request) {
        SubTodoList subTodo = subTodoListRepository.findById(subTodoId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 SubTodo ID입니다: " + subTodoId));

        // 1. 상위 Todo의 소유자를 통해 권한 확인
        TodoList parentTodo = todoListRepository.findById(subTodo.getTodoListId())
                .orElseThrow(() -> new IllegalStateException("상위 Todo 항목이 존재하지 않습니다.")); // 논리적 오류

        if (!parentTodo.getUserId().equals(userId)) {
            throw new SecurityException("SubTodo를 수정할 권한이 없습니다.");
        }

        // 2. 내용 업데이트
        subTodo.setTitle(request.getTitle());

        // @Transactional에 의해 자동 저장

        return SubTodoResponse.fromEntity(subTodo);
    }

    /**
     * SubTodo 항목을 삭제합니다.
     *
     * @param userId 사용자 ID (권한 확인용)
     * @param subTodoId 삭제할 SubTodo 항목 고유 ID
     */
    @Transactional
    public void deleteSubTodo(String userId, Integer subTodoId) {
        SubTodoList subTodo = subTodoListRepository.findById(subTodoId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 SubTodo ID입니다: " + subTodoId));

        // 1. 상위 Todo의 소유자를 통해 권한 확인
        TodoList parentTodo = todoListRepository.findById(subTodo.getTodoListId())
                .orElseThrow(() -> new IllegalStateException("상위 Todo 항목이 존재하지 않습니다.")); // 논리적 오류

        if (!parentTodo.getUserId().equals(userId)) {
            throw new SecurityException("SubTodo를 삭제할 권한이 없습니다.");
        }

        // 2. 삭제
        subTodoListRepository.delete(subTodo);
    }
}