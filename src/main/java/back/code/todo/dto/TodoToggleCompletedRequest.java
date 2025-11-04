package back.code.todo.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TodoToggleCompletedRequest {

    // 토글할 할 일 ID (필수)
    private Integer todoId;         // Todo 항목 고유 ID

    // 변경할 완료 상태 값 (필수)
    private Boolean isCompleted;    // 완료 상태 (true 또는 false)
}