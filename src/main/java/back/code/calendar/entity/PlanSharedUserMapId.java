package back.code.calendar.entity;

import jakarta.persistence.Embeddable;
import lombok.*;
import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PlanSharedUserMapId implements Serializable {

    private Long planId;
    private String sharedUserId;
}

