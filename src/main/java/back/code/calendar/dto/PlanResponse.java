package back.code.calendar.dto;

import back.code.calendar.entity.PlanEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class PlanResponse {
    private Long planId;
    private String planTitle;
    private String planContent;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String color;
    private String repeatType;
    private String reminder;
    private Long folderId;
    private String folderName;

    public static PlanResponse fromEntity(PlanEntity e) {
        return PlanResponse.builder()
                .planId(e.getPlanId())
                .planTitle(e.getPlanTitle())
                .planContent(e.getPlanContent())
                .startTime(e.getStartTime())
                .endTime(e.getEndTime())
                .repeatType(e.getRepeatType())
                .color(e.getColor())
                .reminder(e.getReminder())
                .folderId(e.getFolder().getFolderId())
                .folderName(e.getFolder() != null ? e.getFolder().getFolderName() : null)
                .build();
    }
}
