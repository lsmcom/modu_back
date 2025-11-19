package back.code.notice.service;

import back.code.calendar.entity.PlanEntity;
import back.code.calendar.entity.PlanShareEntity;
import back.code.calendar.entity.PlanSharedUserMapId;
import back.code.calendar.repository.PlanRepository;
import back.code.calendar.repository.PlanShareRepository;
import back.code.notice.entity.Notification;
import back.code.notice.repository.NotificationRepository;
import back.code.notice.dto.NotificationResponse;
import back.code.user.entity.UserEntity;
import back.code.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final PlanRepository planRepository;
    private final UserRepository userRepository;
    private final PlanShareRepository planShareRepository;

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

    /** 일정 공유 "초대" 알림 발송 (PlanShare는 만들지 않음) */
    @Transactional
    public void sendPlanShareRequestNotification(
            String targetUserId, String senderId, Long planId, String title) {

        Notification noti = new Notification();
        noti.setUserId(targetUserId);      // 알림 받는 사람
        noti.setSenderId(senderId);        // 초대한 사람
        noti.setPlanId(planId);            // 수락 시 사용할 일정 ID
        noti.setType(Notification.NotificationType.planshare_request);
        noti.setTitle("[일정 공유 초대] " + title);
        noti.setContent("일정 공유 초대가 도착했습니다. 수락 또는 거절할 수 있습니다.");
        noti.setIsRead(false);

        notificationRepository.save(noti);
    }

    /** 일정 공유 초대 수락: 여기에서 PlanShareEntity 저장 */
    @Transactional
    public void acceptPlanShare(Long notificationId, String userId) {

        Notification noti = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다."));

        if (!noti.getUserId().equals(userId)) {
            throw new SecurityException("권한이 없습니다.");
        }

        if (noti.getType() != Notification.NotificationType.planshare_request) {
            throw new IllegalStateException("일정 공유 초대 알림이 아닙니다.");
        }

        Long planId = noti.getPlanId();
        if (planId == null) {
            throw new IllegalStateException("알림에 연결된 일정 정보가 없습니다.");
        }

        PlanEntity plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다."));

        UserEntity sharedUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 이미 수락되어 있는지(중복 저장 방지)
        boolean exists = planShareRepository.existsById(
                new PlanSharedUserMapId(planId, userId)
        );
        if (!exists) {
            PlanShareEntity share = PlanShareEntity.builder()
                    .id(new PlanSharedUserMapId(planId, userId))
                    .plan(plan)
                    .sharedUser(sharedUser)
                    .build();
            planShareRepository.save(share);
        }

        // 알림 상태 변경
        noti.setIsRead(true);
        noti.setType(Notification.NotificationType.planshare_accept); // 선택적: 상태 표현용
    }

    /** 일정 공유 초대 거절 (PlanShare 저장 없이 읽음 처리만) */
    @Transactional
    public void rejectPlanShare(Long notificationId, String userId) {

        Notification noti = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다."));

        if (!noti.getUserId().equals(userId)) {
            throw new SecurityException("권한이 없습니다.");
        }

        noti.setIsRead(true);
        noti.setType(Notification.NotificationType.planshare_reject); // 선택적
    }

    @Transactional
    public void sendAnnouncementNotification(
            String title,
            String content,
            Long referenceId,
            Notification.NotificationType type
    ) {

        List<UserEntity> allUsers = userRepository.findAll();

        List<Notification> notifications = allUsers.stream()
                .map(user -> {
                    Notification n = new Notification();
                    n.setUserId(user.getUserId());
                    n.setSenderId("ADMIN");
                    n.setReferenceId(referenceId);
                    n.setType(type);      // 공지 타입 설정
                    n.setContent(content);
                    n.setIsRead(false);
                    // 타입에 따라 타이틀 prefix 정확히 구분
                    switch (type) {
                        case community_announcement:
                            n.setTitle("[커뮤니티 공지사항] " + title);
                            break;

                        case inquiry_announcement:
                            n.setTitle("[문의 공지사항] " + title);
                            break;

                        default:
                            n.setTitle("[공지사항] " + title);
                    }
                    return n;
                })
                .toList();

        notificationRepository.saveAll(notifications);
    }

    public void sendCommentNotification(
            UserEntity receiver,   // 알림 받을 사람 (게시글 작성자)
            UserEntity sender,     // 알림 보낸 사람 (댓글 작성자)
            Long postId,           // referenceId
            String postTitle       // 게시글 제목
    ) {

        Notification notification = new Notification();

        notification.setUserId(receiver.getUserId());     // 알림 받을 userId
        notification.setSenderId(sender.getUserId());     // 댓글 단 userId
        notification.setReferenceId(postId);              // 게시글 이동용
        notification.setType(Notification.NotificationType.comment);
        notification.setTitle("[댓글] 새로운 댓글이 달렸습니다.");

        notification.setContent(
                String.format("%s님이 회원님의 게시글 \"%s\"에 댓글을 작성했습니다.",
                        sender.getUserId(),
                        postTitle)
        );

        notification.setIsRead(false);                   // 처음 생성은 읽지 않음

        notificationRepository.save(notification);
    }

    /** 알림 삭제 */
    @Transactional
    public NotificationResponse noticeDelete(long notificationId) throws Exception{

        Notification notice = notificationRepository.findById(notificationId)
                            .orElseThrow(()-> new RuntimeException("알림을 찾을 수 없습니다."));

        if(!notice.getUserId().equals(notice.getUserId())) {
            throw new RuntimeException("삭제 권한이 없습니다.");
        }   

        NotificationResponse response = NotificationResponse.fromEntity(notice);

        notificationRepository.delete(notice);

        return response;
    }

}