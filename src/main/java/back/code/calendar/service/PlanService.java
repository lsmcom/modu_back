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
import back.code.user.entity.UserEntity;
import back.code.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PlanService {

    private final PlanRepository planRepository;
    private final PlanShareRepository planShareRepository;
    private final CalendarFolderRepository calendarFolderRepository;
    private final UserRepository userRepository;
    private final PlanRepeatRuleService repeatRuleService; //  반복 규칙 서비스 추가

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
        PlanEntity plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다. planId=" + planId));

        plan.setPlanTitle(request.getPlanTitle());
        plan.setPlanContent(request.getPlanContent());
        plan.setStartTime(request.getStartTime());
        plan.setEndTime(request.getEndTime());
        plan.setColor(request.getColor());
        plan.setRepeatType(request.getRepeatType());
        plan.setReminder(request.getReminder());

        // folderId로 실제 폴더 엔티티 조회
        CalendarFolderEntity folder = calendarFolderRepository.findById(request.getFolderId())
                .orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다. folderId=" + request.getFolderId()));
        plan.setFolder(folder);

        // userId로 실제 유저 엔티티 조회
        UserEntity user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. userId=" + request.getUserId()));
        plan.setUser(user);

        //  반복 규칙 갱신 (없음 → 반복 / 반복 → 없음)
        repeatRuleService.updateRepeatRule(plan, request.getRepeatType());

        return planRepository.saveAndFlush(plan);
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
        return planRepository.findByFolder_FolderId(folderId)
                .stream()
                .map(PlanResponse::fromEntity)
                .toList();
    }

    /** 사용자별 일정 전체 목록 */
    @Transactional(readOnly = true)
    public List<PlanEntity> getPlansByUser(UserEntity user) {
        return planRepository.findByUserWithFolder(user);
    }

    /** 공유 사용자 추가 */
    public void addSharedUser(PlanEntity plan, UserEntity sharedUser) {
        PlanShareEntity share = PlanShareEntity.builder()
                .id(new PlanSharedUserMapId(plan.getPlanId(), sharedUser.getUserId()))
                .plan(plan)
                .sharedUser(sharedUser)
                .build();
        planShareRepository.save(share);
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
}
