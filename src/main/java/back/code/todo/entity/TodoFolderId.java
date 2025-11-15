package back.code.todo.entity;

import java.io.Serializable;
import java.util.Objects;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * TodoFolder 엔티티의 복합 기본 키를 정의하는 클래스.
 * DB 스키마: PRIMARY KEY (user_id, folder_id)
 */
@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TodoFolderId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "folder_id", nullable = false)
    private Integer folderId; // 폴더 고유 ID

    @Column(name = "user_id", nullable = false)
    private String userId; // 사용자 ID (FK)

    // equals()와 hashCode() 구현 필수
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TodoFolderId that = (TodoFolderId) o;
        return Objects.equals(folderId, that.folderId) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(folderId, userId);
    }
}