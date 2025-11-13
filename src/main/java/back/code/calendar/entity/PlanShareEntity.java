package back.code.calendar.entity;

import back.code.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "plan_share")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanShareEntity {

    @EmbeddedId
    private PlanSharedUserMapId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("planId")
    @JoinColumn(name = "plan_id")
    private PlanEntity plan;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "shared_user_id")
    private UserEntity sharedUser;
}
