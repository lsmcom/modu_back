package back.code.memo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "memo_folder")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemoFolderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "folder_id")
    private Integer folderId;

    @Column(name = "folder_name", nullable = false)
    private String folderName;

    // 폴더 삭제 시 해당 메모도 같이 삭제 (JPA Cascade)
    @OneToMany(mappedBy = "folder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemoEntity> memos;

}
