package back.code.todo.dto;

import back.code.todo.entity.TodoList;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class TodoResponse {

    private Integer todoId;         // 할 일 항목 고유 ID
    private String userId;          // 사용자 ID

    // 폴더 정보 (UI에서 필요)
    private Integer folderId;       // 폴더 ID
    private String folderName;      // 폴더 이름 (TodoFolder 테이블과의 조인 결과)

    private String title;           // 할 일 제목
    private String subTitle;        // 부제 또는 상세 설명
    private Boolean tdFixed;        // 고정 핀 상태 (Todo.js의 todo.td_fixed에 매핑)
    private Boolean isCompleted;    // 완료 상태 (Todo.js의 todo.is_completed에 매핑)
    private Instant dueDate;        // 마감 일시 (Instant 타입)
    private Integer orderIndex;     // 정렬 순서 인덱스

    private Instant createDate;     // 생성 일시

    // WriteTodo.js에서 추가된 필드
    private String repeatDays;      // 반복 요일 (콤마로 구분된 문자열)
    private Boolean autoMigrate;    // 익일 자동 이월 여부

    // 엔티티와 폴더 이름을 기반으로 DTO를 생성하는 편의 메소드
    public static TodoResponse fromEntity(TodoList todoList, String folderName) {
        return TodoResponse.builder()
                .todoId(todoList.getTodoId())
                .userId(todoList.getUserId())
                .folderId(todoList.getFolderId())
                .folderName(folderName) // 조인을 통해 얻은 이름
                .title(todoList.getTitle())
                .subTitle(todoList.getSubTitle())
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