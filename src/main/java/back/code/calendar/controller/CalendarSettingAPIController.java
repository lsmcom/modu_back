package back.code.calendar.controller;

import back.code.calendar.entity.CalendarSettingEntity;
import back.code.calendar.service.CalendarSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/calendar/setting")
@RequiredArgsConstructor
public class CalendarSettingAPIController {

    private final CalendarSettingService settingService;

    /** 사용자별 설정 조회 */
    @GetMapping("/{userId}")
    public ResponseEntity<CalendarSettingEntity> getSetting(@PathVariable String userId) {
        return ResponseEntity.ok(settingService.getSetting(userId));
    }

    /** 설정 저장 / 즉시 반영 */
    @PostMapping("/{userId}/save")
    public ResponseEntity<CalendarSettingEntity> saveSetting(
            @PathVariable String userId,
            @RequestBody CalendarSettingEntity req
    ) {
        return ResponseEntity.ok(settingService.saveSetting(userId, req));
    }
}
