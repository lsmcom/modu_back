package back.code.todo.entity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "todolist")
@Getter
@Setter
@NoArgsConstructor
public class TodoList {

    // @Id: Primary Key (todo_id INT PRIMARY KEY NOT NULL AUTO_INCREMENT)
    // @GeneratedValue: AUTO_INCREMENT 설정
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "todo_id", nullable = false)
    private Integer todoId; // 할 일 항목 고유 ID

    // user_id (VARCHAR(255) NOT NULL) - Foreign Key
    @Column(name = "user_id", nullable = false)
    private String userId; // 사용자 ID (FK)

    // folder_id (INT NOT NULL) - Foreign Key
    @Column(name = "folder_id", nullable = false)
    private Integer folderId; // 폴더 ID (FK)

    // title (VARCHAR(255) NOT NULL)
    @Column(name = "title", nullable = false)
    private String title; // 할 일 제목

    // td_fixed (TINYINT(1) NOT NULL)
    @Column(name = "td_fixed", nullable = false)
    private Boolean tdFixed; // 고정 핀 상태

    // is_completed (TINYINT(1) NOT NULL)
    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted; // 완료 상태

    // due_date (DATETIME NULL)
    @Column(name = "due_date")
    private LocalDateTime dueDate; // 마감일

    // order_index (INT NOT NULL)
    @Column(name = "order_index", nullable = false)
    private Integer orderIndex; // 정렬 순서 인덱스

    // create_date (DATETIME NOT NULL)
    @Column(name = "create_date", nullable = false)
    private LocalDateTime createDate; // 생성 일시

    // repeat_days (VARCHAR(255) NULL)
    @Column(name = "repeat_days")
    private String repeatDays; // 반복 요일 (콤마로 구분된 문자열)

    // auto_migrate (TINYINT(1) NULL)
    @Column(name = "auto_migrate")
    private Boolean autoMigrate; // 익일 자동 이월 여부

    /*
     * N:1 관계 매핑 (TodoList : User, TodoList : TodoFolder).
     * private User user;
     * private TodoFolder folder;
     */
}