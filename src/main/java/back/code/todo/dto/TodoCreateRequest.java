package back.code.todo.dto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class TodoCreateRequest {

    // WriteTodo.js의 title에 매핑
    private String title;           // 할 일 제목 (필수)

    // WriteTodo.js의 finalFolderId에 매핑
    private Integer folderId;       // 할 일을 저장할 폴더 ID (필수)

    // WriteTodo.js의 subTitleContent에 매핑 (sub_title: String)
    private String subTitle;        // 부제 또는 상세 설명

    // WriteTodo.js의 dateTime.date에 매핑 (due_date: Instant)
    private Instant dueDate;        // 마감 일시 (Instant 타입 사용 권장)

    // WriteTodo.js의 td_fixed에 매핑
    private Boolean tdFixed = false; // 고정(핀) 상태 (기본값 false)

    // WriteTodo.js의 autoMigrate에 매핑
    private Boolean autoMigrate;    // 익일 자동 이월 여부

    // WriteTodo.js의 dateTime.repeatDays에 매핑 (String으로 변환되어 서버로 전송)
    private String repeatDays;      // 반복 요일 (콤마로 구분된 문자열)
}