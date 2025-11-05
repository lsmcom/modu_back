package back.code.memo.entity;

import back.code.file.entity.FileEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "file_memo_mapping")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileMemoMapEntity {

    @EmbeddedId
    private FileMemoMapId id = new FileMemoMapId();

    /* 메모 (FK) */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("memoId")
    @JoinColumn(name = "memo_id", nullable = false)
    private MemoEntity memo;

    /* 파일 (FK) */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("fileId")
    @JoinColumn(name = "file_id", nullable = false)
    private FileEntity file;
}

