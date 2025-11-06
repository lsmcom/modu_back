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
@ToString(exclude = {"plan", "sharedUser"})
public class PlanShareEntity {

    @EmbeddedId
    private PlanSharedUserMapId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("planId")
    @JoinColumn(name = "plan_id",
            foreignKey = @ForeignKey(name = "fk_plan_share_plan"))
    private PlanEntity plan;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("sharedUserId")
    @JoinColumn(name = "shared_user_id",
            foreignKey = @ForeignKey(name = "fk_plan_share_user"))
    private UserEntity sharedUser;
}
