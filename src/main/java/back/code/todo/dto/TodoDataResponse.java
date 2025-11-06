package back.code.todo.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TodoDataResponse {

    // 전체 Todo 항목 목록
    private List<TodoResponse> todos;

    // 전체 폴더 목록 (Not Todo 폴더 포함)
    private List<FolderResponse> folders;
}