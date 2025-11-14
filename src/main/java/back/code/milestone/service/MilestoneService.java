package back.code.milestone.service;

import back.code.milestone.dto.UserMilestoneResponse;
import back.code.milestone.entity.Milestone;
import back.code.notice.entity.Notification;
import back.code.milestone.entity.UserMilestone;
import back.code.milestone.repository.MilestoneRepository;
import back.code.milestone.repository.UserMilestoneRepository;
import back.code.notice.repository.NotificationRepository;
import back.code.todo.repository.TodoListRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MilestoneService {

    private final TodoListRepository todoListRepository;
    private final MilestoneRepository milestoneRepository;
    private final UserMilestoneRepository userMilestoneRepository;
    private final back.code.notice.repository.NotificationRepository notificationRepository;

    // 1. 생성자 주입
    public MilestoneService(TodoListRepository todoListRepository,
                            MilestoneRepository milestoneRepository,
                            UserMilestoneRepository userMilestoneRepository,
                            NotificationRepository notificationRepository) {
        this.todoListRepository = todoListRepository;
        this.milestoneRepository = milestoneRepository;
        this.userMilestoneRepository = userMilestoneRepository;
        this.notificationRepository = notificationRepository;
    }

    /**
     * Todo 완료 개수 기반 업적을 확인하고 달성 시 기록 및 알림을 생성합니다.
     * @param userId 업적을 확인할 사용자 ID
     * @return 새로 달성된 Milestone 목록
     */
    @Transactional
    public List<Milestone> checkAndAwardMilestones(String userId) {
        // 1. 현재 사용자의 총 완료 Todo 개수 조회
        long totalCompletedCount = todoListRepository.countCompletedTodosByUserId(userId);

        // 2. 현재 완료 개수보다 기준이 낮거나 같은, 미달성 업적 목록 조회
        List<Milestone> potentialMilestones = milestoneRepository
                .findUnachievedMilestones(userId, (int)totalCompletedCount, "TODO_COMPLETE");

        List<Milestone> newlyAchievedMilestones = new ArrayList<>();

        for (Milestone milestone : potentialMilestones) {
            if (totalCompletedCount >= milestone.getTargetValue()) {
                // 3. 업적 달성 기록 저장
                UserMilestone userMilestone = new UserMilestone(userId, milestone.getMilestoneId());
                userMilestone.setMilestone(milestone);
                userMilestoneRepository.save(userMilestone);

                newlyAchievedMilestones.add(milestone);

                // 4. 업적 달성 알림 생성 및 저장 (notification 테이블)
                createAchievementNotification(userId, milestone);
            }
        }

        return newlyAchievedMilestones;
    }

    /**
     * 업적 달성 시 알림 테이블에 레코드를 생성합니다.
     */
    private void createAchievementNotification(String userId, Milestone milestone) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        // ENUM 타입이 '업적'으로 정의되어 있다고 가정
        notification.setType(Notification.NotificationType.milestone);
        notification.setMilestoneId(milestone.getMilestoneId());
        notification.setTitle("🏆 업적 달성: " + milestone.getName());
        notification.setContent(milestone.getDescription());
        notification.setIsRead(false);

        notificationRepository.save(notification);
    }

    /**
     * 특정 사용자가 달성한 모든 업적 목록을 조회합니다.
     * @param userId 사용자 ID
     * @return 달성된 업적 목록 DTO
     */
    @Transactional(readOnly = true)
    public List<UserMilestoneResponse> getUserMilestonesByUserId(String userId) {
        // 1. Repository에서 UserMilestone과 Milestone 정보를 JOIN FETCH로 한 번에 조회
        List<UserMilestone> userMilestones = userMilestoneRepository.findByUserIdWithMilestone(userId);

        // 2. 조회된 엔티티 목록을 DTO로 변환
        return userMilestones.stream()
                .map(userMilestone -> UserMilestoneResponse.fromEntity(
                        userMilestone,
                        userMilestone.getMilestone()) // UserMilestone 엔티티에 추가된 milestone 필드를 사용
                )
                .collect(Collectors.toList());
    }
}