package back.code.todo.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SubTodoCreateRequest {

    // 클라이언트에서 이 SubTodo가 속할 상위 Todo 항목 ID를 전달받음
    private Integer todoListId; // 상위 할 일 항목 ID (필수)

    // SubTodo의 내용
    private String title;       // 하위 할 일 내용 (필수)
}