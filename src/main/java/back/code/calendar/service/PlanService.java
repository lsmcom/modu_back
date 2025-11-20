package back.code.calendar.service;

import back.code.calendar.dto.PlanCreateRequestDTO;
import back.code.calendar.dto.PlanResponse;
import back.code.calendar.dto.PlanUpdateRequestDTO;
import back.code.calendar.entity.CalendarFolderEntity;
import back.code.calendar.entity.PlanEntity;
import back.code.calendar.entity.PlanShareEntity;
import back.code.calendar.entity.PlanSharedUserMapId;
import back.code.calendar.repository.CalendarFolderRepository;
import back.code.calendar.repository.PlanRepository;
import back.code.calendar.repository.PlanShareRepository;
import back.code.notice.service.NotificationService;
import back.code.user.entity.UserEntity;
import back.code.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional
public class PlanService {

    private final PlanRepository planRepository;
    private final PlanShareRepository planShareRepository;
    private final CalendarFolderRepository calendarFolderRepository;
    private final UserRepository userRepository;
    private final PlanRepeatRuleService repeatRuleService; //  반복 규칙 서비스 추가
    private final NotificationService notificationService;

    /** 일정 등록 */
    @Transactional
    public PlanResponse createPlan(PlanCreateRequestDTO request) {

        CalendarFolderEntity folder = calendarFolderRepository.findById(request.getFolderId())
                .orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다. folderId=" + request.getFolderId()));

        UserEntity user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. userId=" + request.getUserId()));

        PlanEntity plan = PlanEntity.builder()
                .planTitle(request.getPlanTitle())
                .planContent(request.getPlanContent())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .color(request.getColor())
                .repeatType(request.getRepeatType())
                .reminder(request.getReminder())
                .folder(folder)
                .user(user)
                .build();

        PlanEntity saved = planRepository.save(plan);

        //  반복 규칙 등록 (없음 제외)
        repeatRuleService.saveRepeatRule(saved, request.getRepeatType());

        return PlanResponse.fromEntity(saved);
    }

    /** 일정 수정 */
    @Transactional
    public PlanEntity updatePlan(Long planId, PlanUpdateRequestDTO request) {

        // 1) 일정 조회 및 수정
        PlanEntity plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다. planId=" + planId));

        plan.setPlanTitle(request.getPlanTitle());
        plan.setPlanContent(request.getPlanContent());
        plan.setStartTime(request.getStartTime());
        plan.setEndTime(request.getEndTime());
        plan.setColor(request.getColor());
        plan.setRepeatType(request.getRepeatType());
        plan.setReminder(request.getReminder());

        CalendarFolderEntity folder = calendarFolderRepository.findById(request.getFolderId())
                .orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다. folderId=" + request.getFolderId()));
        plan.setFolder(folder);

        UserEntity user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. userId=" + request.getUserId()));
        plan.setUser(user);

        repeatRuleService.updateRepeatRule(plan, request.getRepeatType());

        PlanEntity saved = planRepository.saveAndFlush(plan);


        // 2) 공유받은 사용자들에게 수정 알림 전송
        List<PlanShareEntity> sharedUsers = planShareRepository.findByPlan(plan);

        for (PlanShareEntity ps : sharedUsers) {
            String targetUserId = ps.getSharedUser().getUserId();

            // 본인에게는 알림 X
            if (targetUserId.equals(user.getUserId())) continue;

            notificationService.sendPlanUpdatedNotification(
                    targetUserId,
                    user.getUserId(),
                    plan.getPlanId(),
                    plan.getPlanTitle()
            );
        }

        return saved;
    }

    /** 일정 삭제 */
    @Transactional
    public void deletePlan(Long planId) {
        //  반복 규칙도 함께 삭제 (ON DELETE CASCADE지만 안전하게 한 번 더)
        repeatRuleService.deleteRepeatRuleByPlan(planId);
        planRepository.deleteById(planId);
    }

    /** 폴더별 일정 목록 */
    @Transactional(readOnly = true)
    public List<PlanResponse> getPlansByFolder(Long folderId) {
        CalendarFolderEntity folder = calendarFolderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("폴더 없음"));

        // 공유 폴더라면 PlanShare에서 가져오기
        if ("SHARED".equals(folder.getFolderType())) {
            List<PlanShareEntity> shared = planShareRepository.findBySharedUser_UserId(folder.getUser().getUserId());
            return shared.stream()
                    .map(PlanShareEntity::getPlan)
                    .map(PlanResponse::fromEntity)
                    .toList();
        }

        // 일반 폴더라면 기존 로직
        return planRepository.findByFolder_FolderId(folderId)
                .stream()
                .map(PlanResponse::fromEntity)
                .toList();
    }

    /** 사용자별 일정 전체 목록 */
    @Transactional(readOnly = true)
    public List<PlanResponse> getPlansByUser(UserEntity user) {
        // 본인 일정
        List<PlanEntity> ownPlans = planRepository.findByUserWithFolder(user);

        // 공유받은 일정 (fetch join으로 계획까지 즉시 로딩)
        List<PlanShareEntity> shared = planShareRepository.findBySharedUserWithPlan(user);
        List<PlanEntity> receivedPlans = shared.stream()
                .map(PlanShareEntity::getPlan)
                .toList();

        // 두 목록 병합
        return Stream.concat(ownPlans.stream(), receivedPlans.stream())
                .distinct()
                .map(PlanResponse::fromEntity)
                .toList();
    }

    /** 일정 공유자 추가: 이제는 "공유 초대 알림"만 발송 */
    @Transactional
    public void addSharedUser(PlanEntity plan, UserEntity sharedUser) {

        PlanEntity realPlan = planRepository.findById(plan.getPlanId())
                .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다."));

        String senderId = realPlan.getUser().getUserId();

        // 예전처럼 PlanShareEntity는 여기서 저장하지 않습니다.
        //  → 수락 API에서만 PlanShareEntity 저장

        notificationService.sendPlanShareRequestNotification(
                sharedUser.getUserId(),          // 초대 받는 사람
                senderId,                        // 초대한 사람
                realPlan.getPlanId(),            // 일정 ID (수락 시 사용)
                realPlan.getPlanTitle()
        );
    }

    /** 일정 공유 초대 요청 보내기 (저장 X) */
    @Transactional
    public void requestShareUser(PlanEntity plan, UserEntity sharedUser) {

        PlanEntity realPlan = planRepository.findById(plan.getPlanId())
                .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다."));

        notificationService.sendPlanShareRequestNotification(
                sharedUser.getUserId(),
                realPlan.getUser().getUserId(),
                realPlan.getPlanId(),
                realPlan.getPlanTitle()
        );
    }

    // 내 일정 + 공유 받은 일정 모두 조회
    @Transactional(readOnly = true)
    public List<PlanResponse> getPlansByUserIncludingShared(String userId) {
        //  본인 일정
        List<PlanEntity> personalPlans = planRepository.findByUser_UserId(userId);

        // 공유된 일정
        List<PlanEntity> sharedPlans = planShareRepository.findPlansSharedWithUser(userId);

        // DTO 변환 시 folderType 강제 변경
        List<PlanResponse> sharedResponses = sharedPlans.stream()
                .map(p -> {
                    PlanResponse r = PlanResponse.fromEntity(p);
                    r.setFolderType("SHARED");
                    r.setOwnerId(p.getUser().getUserId());  // 일정 원래 작성자
                    return r;
                })
                .toList();

        // 합치기
        List<PlanResponse> personalResponses = personalPlans.stream()
                .map(PlanResponse::fromEntity)
                .toList();

        return Stream.concat(personalResponses.stream(), sharedResponses.stream())
                .distinct()
                .toList();
    }

    /** 공유 사용자 삭제 */
    public void removeSharedUser(PlanEntity plan, UserEntity sharedUser) {
        planShareRepository.deleteById(new PlanSharedUserMapId(plan.getPlanId(), sharedUser.getUserId()));
    }

    /** 일정의 공유자 목록 */
    @Transactional(readOnly = true)
    public List<PlanShareEntity> getSharedUsers(PlanEntity plan) {
        return planShareRepository.findByPlan(plan);
    }

    @Transactional
    public void updateSharedPlanColors(String userId, String newColor) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 사용자의 공유 폴더 목록 조회
        List<CalendarFolderEntity> sharedFolders = calendarFolderRepository.findByUserAndFolderType(user, "SHARED");

        // 공유 폴더에 포함된 모든 일정 색상 업데이트
        for (CalendarFolderEntity folder : sharedFolders) {
            List<PlanEntity> plans = planRepository.findByFolder(folder);
            for (PlanEntity plan : plans) {
                plan.setColor(newColor);
            }
        }
    }

    @Transactional(readOnly = true)
    public List<PlanResponse> getAllPlansByUser(String userId) {

        // 1. 내가 만든 일정
        List<PlanEntity> myPlans = planRepository.findByUser_UserId(userId);

        // 2. 내가 공유받은 일정 (plan, folder 즉시 로딩)
        List<PlanShareEntity> sharedPlans = planShareRepository.findBySharedUser_UserId(userId);

        // 3. 공유받은 사람 자신의 "SHARED 폴더" 조회
        CalendarFolderEntity mySharedFolder =
                calendarFolderRepository.findByUser_UserIdAndFolderType(userId, "SHARED");

        if (mySharedFolder == null) {
            throw new IllegalStateException("공유 폴더가 존재하지 않습니다. userId=" + userId);
        }

        List<PlanResponse> result = new ArrayList<>();

        // 4. 내가 만든 일정 → 그대로 변환
        for (PlanEntity p : myPlans) {
            result.add(PlanResponse.fromEntity(p));
        }

        // 5. 내가 공유받은 일정 → 내 공유 폴더 기준으로 덮어쓰기
        for (PlanShareEntity sp : sharedPlans) {
            PlanEntity p = sp.getPlan();

            PlanResponse dto = PlanResponse.fromEntity(p);

            dto.setFolderId(mySharedFolder.getFolderId());
            dto.setFolderName(mySharedFolder.getFolderName());
            dto.setFolderType("SHARED");

            result.add(dto);
        }

        return result;
    }

}
