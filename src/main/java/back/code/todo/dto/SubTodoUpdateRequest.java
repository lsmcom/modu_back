package back.code.todo.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SubTodoUpdateRequest {

    // 수정할 하위 할 일 항목 고유 ID
    private Integer id;         // 하위 할 일 항목 고유 ID (필수)

    // 수정할 내용
    private String title;       // 새 하위 할 일 내용 (필수)
}