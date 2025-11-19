package back.code.admin.dto.report;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminReportStatusUpdateDTO {
    private String status; // "PENDING", "APPROVED", "REJECTED"
}
