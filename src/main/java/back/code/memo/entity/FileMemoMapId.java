package back.code.memo.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class FileMemoMapId implements Serializable {
    private Integer memoId;
    private String fileId;
}
