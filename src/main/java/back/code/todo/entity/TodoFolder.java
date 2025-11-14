package back.code.todo.entity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "todofolder")
@Getter
@Setter
@NoArgsConstructor
@IdClass(TodoFolderId.class)
public class TodoFolder {

    @Id
    @Column(name = "folder_id", nullable = false)
    private Integer folderId; // 폴더 고유 ID

    @Id
    @Column(name = "user_id", nullable = false)
    private String userId; // 사용자 ID (FK)

    @Column(name = "name", nullable = false)
    private String name; // 폴더 이름

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault;
}