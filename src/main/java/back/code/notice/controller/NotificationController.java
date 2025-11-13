package back.code.notice.controller;

import back.code.notice.dto.NotificationResponse;
import back.code.notice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    // private static final String TEMP_USER_ID = "testUser";

    /**
     * 사용자의 모든 알림 목록을 최신순으로 조회합니다. (알림 페이지 메인)
     * GET /api/notifications
     * @param userId 요청 헤더에서 추출된 사용자 ID
     */
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            @RequestHeader("X-User-Id") String userId) {

        List<NotificationResponse> notifications = notificationService.getNotificationsByUserId(userId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * 사용자의 읽지 않은 알림 개수를 조회합니다. (헤더 뱃지 표시용)
     * GET /api/notifications/count/unread
     * @param userId 요청 헤더에서 추출된 사용자 ID
     */
    @GetMapping("/count/unread")
    public ResponseEntity<Long> getUnreadNotificationCount(
            @RequestHeader("X-User-Id") String userId) {

        long count = notificationService.getUnreadNotificationCount(userId);
        return ResponseEntity.ok(count);
    }

    /**
     * 특정 알림을 읽음 처리합니다.
     * PATCH /api/notifications/{notificationId}/read
     * @param userId 요청 헤더에서 추출된 사용자 ID
     */
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Long notificationId) {

        NotificationResponse updatedNotification = notificationService.markNotificationAsRead(notificationId, userId); // 🔄 수정: userId 인자 사용
        return ResponseEntity.ok(updatedNotification);
    }
}