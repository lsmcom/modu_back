package back.code.admin.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUserStatusUpdateDTO {
    private String status;   // active, inactive, withdrawn
}
