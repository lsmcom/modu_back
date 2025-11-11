package back.code.calendar.entity;

import back.code.user.entity.UserEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "calendar_setting")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "user")
public class CalendarSettingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "setting_id")
    private Long settingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_calendar_setting_user"))
    @JsonIgnore
    private UserEntity user;

    @Column(name = "enable_notification", length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
    private String enableNotification = "N";

    @Column(name = "share_plan_color", length = 10, columnDefinition = "VARCHAR(10) DEFAULT '#A9EDED'")
    private String sharePlanColor = "#A9EDED";

    @Column(name = "default_view", length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'dayGridMonth'")
    private String defaultView = "dayGridMonth";

    @Column(name = "time_zone", length = 50, columnDefinition = "VARCHAR(50) DEFAULT 'Asia/Seoul'")
    private String timeZone = "Asia/Seoul";

    @Column(name = "show_repeat_plan", length = 1, columnDefinition = "CHAR(1) DEFAULT 'Y'")
    private String showRepeatPlan = "Y";
}
