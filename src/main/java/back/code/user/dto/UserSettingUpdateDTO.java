package back.code.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserSettingUpdateDTO {
    private String theDayOfWeek;       // M or S
    private String alarmAllowed;       // Y / N
    private String personalInfoAgreed;       // Y / N
    private String locationInfoAgreed;       // Y / N
    private String marketingInfoAgreed;// Y / N
}
