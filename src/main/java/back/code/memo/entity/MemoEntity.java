package back.code.memo.entity;

import back.code.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "memo")
@Getter
@Setter
public class MemoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "memo_id")
    private Integer memoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")
    private MemoFolderEntity folder;

    @Column(name = "memo_title")
    private String memoTitle;

    @Column(name = "memo_contents", columnDefinition = "TEXT")
    private String memoContents;

    @Column(name = "is_fixed", columnDefinition = "CHAR(1)")
    private String isFixed;

    @Column(name = "create_date")
    private LocalDateTime createDate;

    @Column(name = "update_date")
    private LocalDateTime updateDate;
}
