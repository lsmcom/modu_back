package back.code.todo.dto;

import back.code.todo.entity.TodoList;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List; // 수정된 부분: List 추가 (subTodos 필드용)

@Getter
@Builder
public class TodoResponse {

    private Integer todoId;         // 할 일 항목 고유 ID
    private String userId;          // 사용자 ID

    // 폴더 정보 (UI에서 필요)
    private Integer folderId;       // 폴더 ID
    private String folderName;      // 폴더 이름 (TodoFolder 테이블과의 조인 결과)

    private String title;           // 할 일 제목
    /* 수정된 부분 시작: subTitle 필드 제거 및 subTodos 리스트 추가 */
//    private String subTitle;        // 부제 또는 상세 설명
    private List<SubTodoResponse> subTodos; // 하위 할 일 목록 (SubTodoList 테이블과의 조인 결과)
    /* 수정된 부분 끝 */
    private Boolean tdFixed;        // 고정 핀 상태 (Todo.js의 todo.td_fixed에 매핑)
    private Boolean isCompleted;    // 완료 상태 (Todo.js의 todo.is_completed에 매핑)
    private Instant dueDate;        // 마감 일시 (Instant 타입)
    private Integer orderIndex;     // 정렬 순서 인덱스

    private Instant createDate;     // 생성 일시

    // WriteTodo.js에서 추가된 필드
    private String repeatDays;      // 반복 요일 (콤마로 구분된 문자열)
    private Boolean autoMigrate;    // 익일 자동 이월 여부

    // 엔티티와 폴더 이름을 기반으로 DTO를 생성하는 편의 메소드
    /* 수정된 부분: subTodos 필드를 추가하는 오버로드된 메소드 추가 */
    public static TodoResponse fromEntity(TodoList todoList, String folderName, List<SubTodoResponse> subTodos) {
        return TodoResponse.builder()
                .todoId(todoList.getTodoId())
                .userId(todoList.getUserId())
                .folderId(todoList.getFolderId())
                .folderName(folderName) // 조인을 통해 얻은 이름
                .title(todoList.getTitle())
//                .subTitle(todoList.getSubTitle()) // subTitle 제거
                .subTodos(subTodos) // subTodos 추가
                .tdFixed(todoList.getTdFixed())
                .isCompleted(todoList.getIsCompleted())
                .dueDate(todoList.getDueDate())
                .orderIndex(todoList.getOrderIndex())
                .createDate(todoList.getCreateDate())
                .repeatDays(todoList.getRepeatDays())
                .autoMigrate(todoList.getAutoMigrate())
                .build();
    }

    // 기존 fromEntity 메소드는 더 이상 사용하지 않지만, 임시로 유지하거나 제거

    @Deprecated
    public static TodoResponse fromEntity(TodoList todoList, String folderName) {
        return TodoResponse.builder()
                .todoId(todoList.getTodoId())
                .userId(todoList.getUserId())
                .folderId(todoList.getFolderId())
                .folderName(folderName)
                .title(todoList.getTitle())
                .subTodos(List.of())
                .tdFixed(todoList.getTdFixed())
                .isCompleted(todoList.getIsCompleted())
                .dueDate(todoList.getDueDate())
                .orderIndex(todoList.getOrderIndex())
                .createDate(todoList.getCreateDate())
                .repeatDays(todoList.getRepeatDays())
                .autoMigrate(todoList.getAutoMigrate())
                .build();
    }
}