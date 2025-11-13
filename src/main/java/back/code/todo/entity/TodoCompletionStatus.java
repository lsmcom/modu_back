package back.code.todo.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "todo_completion_status")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TodoCompletionStatus {

    // user_id는 통계 테이블의 기본 키이자, FK 역할을 수행 (String으로 매핑)
    @Id
    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @Column(name = "completed_count", nullable = false)
    private Integer completedCount = 0;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated = LocalDateTime.now();

    // 편의 생성자 (최초 생성 시 사용)
    public TodoCompletionStatus(String userId, Integer completedCount) {
        this.userId = userId;
        this.completedCount = completedCount;
        this.lastUpdated = LocalDateTime.now();
    }
}