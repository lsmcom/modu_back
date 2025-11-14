package back.code.milestone.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "milestone")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Milestone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "milestone_id")
    private Long milestoneId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "target_type", nullable = false, length = 50)
    private String targetType; // 예: TODO_COMPLETE

    @Column(name = "target_value", nullable = false)
    private Integer targetValue; // 예: 10, 20, 30

    @Column(name = "reward", length = 255)
    private String reward;

    @Column(name = "is_hidden", nullable = false)
    private Boolean isHidden = false;

    // DB 기본값 설정은 JPA 엔티티에서 명시하지 않고 DB 스키마에 의존합니다.
    @Column(name = "create_date", nullable = false)
    private LocalDateTime createDate;
}