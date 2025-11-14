package back.code.memo.entity;

import back.code.memo.enums.FolderType;
import back.code.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "memo_folder")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemoFolderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer folderId;

    @Column(nullable = false)
    private String folderName;

    @Enumerated(EnumType.STRING)
    private FolderType folderType; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
}
