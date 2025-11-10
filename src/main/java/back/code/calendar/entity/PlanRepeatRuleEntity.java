package back.code.calendar.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "plan_repeat_rule")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanRepeatRuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rule_id")
    private Long ruleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_plan_repeat_rule_plan"))
    private PlanEntity plan;

    @Column(name = "repeat_type", length = 10, nullable = false)
    private String repeatType; // 매일 / 매주 / 매월
}
