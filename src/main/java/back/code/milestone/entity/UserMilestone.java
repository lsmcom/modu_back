package back.code.milestone.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_milestone")
@Getter
@Setter
@NoArgsConstructor
public class UserMilestone {

    // 1. 복합 키 매핑
    @EmbeddedId
    private UserMilestoneId id;

    // 3. Milestone 엔티티 관계 추가 (업적 상세 정보 조회에 필수)
    @MapsId("milestoneId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "milestone_id")
    private Milestone milestone;

    @Column(name = "achieved_at", nullable = false)
    private LocalDateTime achievedAt = LocalDateTime.now();

    // 편의 생성자
    public UserMilestone(String userId, Long milestoneId) {
        this.id = new UserMilestoneId(userId, milestoneId);
        this.achievedAt = LocalDateTime.now();
    }
    // 서비스 레이어에서 userId 접근을 위해 Getter를 추가
    public String getUserId() {
        return this.id.getUserId();
    }
}