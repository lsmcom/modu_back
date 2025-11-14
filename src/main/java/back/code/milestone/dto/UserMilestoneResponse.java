package back.code.milestone.dto;

import back.code.milestone.entity.Milestone;
import back.code.milestone.entity.UserMilestone;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class UserMilestoneResponse {

    // 1. 달성 기록 정보 (UserMilestone)
    private Long milestoneId;
    private LocalDateTime achievedAt; // 달성 시간

    // 2. 업적 상세 정보 (Milestone)
    private String name;        // 마일스톤명
    private String description; // 마일스톤 설명
    private Integer targetValue; // 목표 값
    private String reward;      // 보상
    private String targetType;  // 타입

    // Mapper 메서드: DB 엔티티 (UserMilestone + Milestone)를 DTO로 변환
    public static UserMilestoneResponse fromEntity(UserMilestone userMilestone, Milestone milestone) {
        return UserMilestoneResponse.builder()
                // UserMilestone 정보
                .milestoneId(userMilestone.getId().getMilestoneId()) // UserMilestone에서 ID를 가져옴
                .achievedAt(userMilestone.getAchievedAt())

                // Milestone 상세 정보
                .name(milestone.getName())
                .description(milestone.getDescription())
                .targetValue(milestone.getTargetValue())
                .reward(milestone.getReward())
                .targetType(milestone.getTargetType())
                .build();
    }
}