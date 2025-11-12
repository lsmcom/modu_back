package back.code.notice.repository;

import back.code.notice.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * 특정 사용자의 모든 알림을 생성일자(createDate)의 최신순(DESC)으로 조회합니다.
     * @param userId 사용자 ID
     * @return 알림 목록 (List<Notification>)
     */
    List<Notification> findByUserIdOrderByCreateDateDesc(String userId);

    /**
     * 특정 사용자의 읽지 않은(isRead = false) 알림 개수를 집계합니다.
     * @param userId 사용자 ID
     * @return 읽지 않은 알림 개수
     */
    long countByUserIdAndIsReadFalse(String userId);

    // 알림 페이지에서 읽음 처리 시 사용: notificationId로 알림을 찾습니다.
    // JpaRepository의 기본 메서드 (findById)로 충분합니다.
}