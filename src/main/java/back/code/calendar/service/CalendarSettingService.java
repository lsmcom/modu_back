package back.code.calendar.service;

import back.code.calendar.entity.CalendarSettingEntity;
import back.code.calendar.repository.CalendarSettingRepository;
import back.code.user.entity.UserEntity;
import back.code.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CalendarSettingService {

    private final CalendarSettingRepository settingRepository;
    private final UserRepository userRepository;

    /** 사용자 설정 조회 (없으면 기본값 생성) */
    @Transactional(readOnly = true)
    public CalendarSettingEntity getSetting(String userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        return settingRepository.findByUser(user)
                .orElseGet(() -> {
                    CalendarSettingEntity defaultSetting = CalendarSettingEntity.builder()
                            .user(user)
                            .sharePlanColor("#A9EDED")
                            .defaultView("dayGridMonth")
                            .timeZone("Asia/Seoul")
                            .build();
                    return settingRepository.save(defaultSetting);
                });
    }

    /** 설정 저장 / 즉시 반영 */
    public CalendarSettingEntity saveSetting(String userId, CalendarSettingEntity req) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        CalendarSettingEntity existing = settingRepository.findByUser(user)
                .orElse(CalendarSettingEntity.builder().user(user).build());

        // 전달된 값들 업데이트
        if (req.getSharePlanColor() != null)
            existing.setSharePlanColor(req.getSharePlanColor());
        if (req.getDefaultView() != null)
            existing.setDefaultView(req.getDefaultView());
        if (req.getTimeZone() != null)
            existing.setTimeZone(req.getTimeZone());
        if (req.getShowRepeatPlan() != null)
            existing.setShowRepeatPlan(req.getShowRepeatPlan());

        return settingRepository.save(existing);
    }
}
