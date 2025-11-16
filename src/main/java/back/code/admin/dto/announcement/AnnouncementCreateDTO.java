package back.code.admin.dto.announcement;

import back.code.common.entity.BaseTimeEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnnouncementCreateDTO {

    private String type;       // NOTICE,  INQUIRY_RESPONSE
    private String title;
    private String content;
    private String userId;
}
