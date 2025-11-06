package back.code.todo.entity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "todofolder")
@Getter
@Setter
@NoArgsConstructor
public class TodoFolder {

    // @Id: Primary Key (folder_id INT PRIMARY KEY NOT NULL)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "folder_id", nullable = false)
    private Integer folderId; // 폴더 고유 ID

    // user_id (VARCHAR(255) NOT NULL) - Foreign Key
    @Column(name = "user_id", length = 255, nullable = false)
    private String userId; // 사용자 ID (FK)

    // name (VARCHAR(255) NOT NULL)
    @Column(name = "name", length = 255, nullable = false)
    private String name; // 폴더 이름

    /*
     * N:1 관계 매핑 (TodoFolder : User).
     * @ManyToOne
     * @JoinColumn(name = "user_id", insertable = false, updatable = false)
     * private User user;
     */
}