package back.code.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserSettingUpdateDTO {
    private String theDayOfWeek;       // M or S
    private String themeMode;          // light / dark
    private String alarmAllowed;       // Y / N
    private String marketingInfoAgreed;// Y / N
}
