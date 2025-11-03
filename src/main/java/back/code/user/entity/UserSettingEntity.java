package back.code.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_setting")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSettingEntity {

    @Id
    private String settingId; // 설정 고유 번호

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user; // 회원 아이디

    @Column(length = 1, columnDefinition = "CHAR(1)")
    private String theDayOfWeek = "M"; // 주시작일 -> M: 월요일, S: 일요일

    private String themeMode = "light"; // 테마 모드 -> light: 기본 모드, dark: 다크모드

    @Column(length = 1, columnDefinition = "CHAR(1)")
    private String alarmAllowed = "Y"; // 알람 허용 여부 -> Y: 허용, N: 미허용

    @Column(length = 1, columnDefinition = "CHAR(1)")
    private String personalInfoAgreed = "Y"; // 개인정보 수집/이용 동의 여부 -> Y: 허용, N: 미허용

    @Column(length = 1, columnDefinition = "CHAR(1)")
    private String locationInfoAgreed = "Y"; // 위치기반 서비스 동의 여부 -> Y: 허용, N: 미허용

    @Column(length = 1, columnDefinition = "CHAR(1)")
    private String marketingInfoAgreed = "Y"; // 마켓팅 정보 수신 동의 여부 -> Y: 허용, N: 미허용

    private LocalDateTime marketingRejectDate; // 마켓팅 정보 수신 동의 변경 날짜

    /** 마케팅 동의 여부 변경 시 처리 */
    public void updateMarketingAgree(String agreeYn) {
        this.marketingInfoAgreed = agreeYn;
        this.marketingRejectDate = LocalDateTime.now();
    }
}
