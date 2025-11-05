package back.code.todo.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
public class TodoUpdateRequest {

    // 수정할 항목의 ID (필수)
    private Integer todoId;         // Todo 항목 고유 ID

    // WriteTodo.js의 title에 매핑
    private String title;           // 할 일 제목

    // WriteTodo.js의 finalFolderId에 매핑
    private Integer folderId;       // 할 일을 저장할 폴더 ID

    // WriteTodo.js의 subTitleContent에 매핑
    private String subTitle;        // 부제 또는 상세 설명

    // 수정 시 기존 상태를 유지하거나 변경할 수 있음
    private Boolean tdFixed;        // 고정(핀) 상태
    private Boolean isCompleted;    // 완료 상태

    // WriteTodo.js의 dateTime.date에 매핑
    private Instant dueDate;        // 마감 일시

    // WriteTodo.js의 autoMigrate에 매핑
    private Boolean autoMigrate;    // 익일 자동 이월 여부

    // WriteTodo.js의 dateTime.repeatDays에 매핑
    private String repeatDays;      // 반복 요일 (콤마로 구분된 문자열)
}