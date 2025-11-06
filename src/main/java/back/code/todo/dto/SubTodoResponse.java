package back.code.todo.dto;

import back.code.todo.entity.SubTodoList;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SubTodoResponse {

    private Integer id;         // 하위 할 일 항목 고유 ID
    private Integer todoListId; // 상위 할 일 항목 ID
    private String title;       // 하위 할 일 내용

    // 엔티티를 기반으로 DTO를 생성하는 편의 메소드
    public static SubTodoResponse fromEntity(SubTodoList subTodo) {
        return SubTodoResponse.builder()
                .id(subTodo.getId())
                .todoListId(subTodo.getTodoListId())
                .title(subTodo.getTitle())
                .build();
    }
}