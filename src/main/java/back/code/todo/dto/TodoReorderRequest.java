package back.code.todo.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TodoReorderRequest {

    // TodoContent.js의 updatedReorderedList 항목에 매핑
    private Integer todoId;      // 순서를 변경할 할 일 ID (필수)
    private Integer orderIndex;  // 할 일이 갖게 될 새로운 정렬 순서 인덱스 (필수)
}