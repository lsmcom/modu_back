package back.code.calendar.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PlanCreateRequestDTO {
    private String planTitle;
    private String planContent;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String color;
    private String repeatType;
    private String reminder;
    private Long folderId;
    private String userId;
}
