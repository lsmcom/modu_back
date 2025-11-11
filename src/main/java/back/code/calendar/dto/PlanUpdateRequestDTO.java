package back.code.calendar.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// 📁 PlanUpdateRequest.java
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlanUpdateRequestDTO {
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

