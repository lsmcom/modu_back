package back.code.memo.entity;

import back.code.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "memo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "memo_id")
    private Integer memoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "userId", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id", nullable = false)
    private MemoFolderEntity folder;

    @Column(name = "memo_title")
    private String memoTitle;

    @Column(name = "memo_contents", columnDefinition = "TEXT")
    private String memoContents;

    @Column(name = "is_fixed", columnDefinition = "CHAR(1)")
    private String isFixed;

    // Builder로 생성해도 null 방지 (기본값 설정)
    @Builder.Default
    @Column(name = "create_date")
    private LocalDateTime createDate = LocalDateTime.now();

    @Builder.Default
    @Column(name = "update_date")
    private LocalDateTime updateDate = LocalDateTime.now();

    @OneToMany(mappedBy = "memo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FileMemoMapEntity> memoFiles = new ArrayList<>();

    // 새 엔티티 생성 시 (INSERT 전에 자동 호출)
    @PrePersist
    public void onCreate() {
        if (this.createDate == null) {
            this.createDate = LocalDateTime.now();
        }
        this.updateDate = LocalDateTime.now();
    }

    // 기존 엔티티 수정 시 (UPDATE 전에 자동 호출)
    @PreUpdate
    public void onUpdate() {
        this.updateDate = LocalDateTime.now();
    }
}
