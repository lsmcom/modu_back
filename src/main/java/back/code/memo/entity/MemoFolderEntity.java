package back.code.memo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "memo_folder")
@Getter
@Setter
public class MemoFolderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "folder_id")
    private Integer folderId;

    @Column(name = "folder_name", nullable = false)
    private String folderName;

}
