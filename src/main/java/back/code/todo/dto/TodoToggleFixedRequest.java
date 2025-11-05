package back.code.todo.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TodoToggleFixedRequest {

    // 상태를 변경할 할 일 ID (필수)
    private Integer todoId; // Todo 항목 고유 ID

    // 변경할 고정 상태 값 (true 또는 false)
    private Boolean tdFixed; // 고정 핀 상태
}
