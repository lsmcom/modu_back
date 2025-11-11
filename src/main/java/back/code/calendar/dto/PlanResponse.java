package back.code.calendar.dto;

import back.code.calendar.entity.PlanEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime startTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime endTime;
    private String color;
    private String repeatType;
    private String reminder;
    private Long folderId;
    private String folderName;
    private String folderType;

    public static PlanResponse fromEntity(PlanEntity e) {
        return PlanResponse.builder()
                .planId(e.getPlanId())
                .planTitle(e.getPlanTitle())
                .planContent(e.getPlanContent())
                .startTime(e.getStartTime())
                .endTime(e.getEndTime())
                .color(e.getColor())
                .repeatType(e.getRepeatType())
                .reminder(e.getReminder())
                .folderId(e.getFolder() != null ? e.getFolder().getFolderId() : null)
                .folderName(e.getFolder() != null ? e.getFolder().getFolderName() : null)
                .folderType(e.getFolder() != null ? e.getFolder().getFolderType() : null)
                .build();
    }
}
