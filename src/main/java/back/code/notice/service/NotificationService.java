package back.code.notice.service;

import back.code.notice.entity.Notification;
import back.code.notice.repository.NotificationRepository;
import back.code.notice.dto.NotificationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * 특정 사용자의 모든 알림 목록을 최신순으로 조회합니다.
     * @param userId 사용자 ID
     * @return 알림 목록 DTO
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationsByUserId(String userId) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreateDateDesc(userId);

        // Notification 엔티티를 클라이언트가 사용할 DTO로 변환
        return notifications.stream()
                .map(NotificationResponse::fromEntity) // NotificationResponse::fromEntity가 구현되어 있다고 가정
                .collect(Collectors.toList());
    }

    /**
     * 특정 사용자의 읽지 않은 알림 개수를 집계합니다.
     * @param userId 사용자 ID
     * @return 읽지 않은 알림 개수
     */
    @Transactional(readOnly = true)
    public long getUnreadNotificationCount(String userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    /**
     * 특정 알림을 읽음 처리합니다.
     * @param notificationId 알림 고유 ID
     * @param userId 권한 확인을 위한 사용자 ID
     * @return 읽음 처리된 NotificationResponse
     */
    @Transactional
    public NotificationResponse markNotificationAsRead(Long notificationId, String userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 알림 ID입니다."));

        if (!notification.getUserId().equals(userId)) {
            throw new SecurityException("알림을 처리할 권한이 없습니다.");
        }

        notification.setIsRead(true);
        Notification savedNotification = notificationRepository.save(notification); // Dirty Checking에 의해 저장되지만 명시적으로 호출

        return NotificationResponse.fromEntity(savedNotification);
    }
}