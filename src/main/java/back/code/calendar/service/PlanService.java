package back.code.calendar.service;

import back.code.calendar.entity.CalendarFolderEntity;
import back.code.calendar.entity.PlanEntity;
import back.code.calendar.entity.PlanShareEntity;
import back.code.calendar.entity.PlanSharedUserMapId;
import back.code.calendar.repository.PlanRepository;
import back.code.calendar.repository.PlanShareRepository;
import back.code.user.entity.UserEntity;
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

    /** 일정 등록 */
    public PlanEntity createPlan(PlanEntity plan) {
        return planRepository.save(plan);
    }

    /** 일정 수정 */
    public PlanEntity updatePlan(String planId, PlanEntity updated) {
        PlanEntity plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다."));
        plan.setPlanTitle(updated.getPlanTitle());
        plan.setPlanContent(updated.getPlanContent());
        plan.setStartTime(updated.getStartTime());
        plan.setEndTime(updated.getEndTime());
        plan.setRepeatType(updated.getRepeatType());
        plan.setColor(updated.getColor());
        plan.setReminder(updated.getReminder());
        return planRepository.save(plan);
    }

    /** 일정 삭제 */
    public void deletePlan(String planId) {
        planRepository.deleteById(planId);
    }

    /** 폴더별 일정 목록 */
    @Transactional(readOnly = true)
    public List<PlanEntity> getPlansByFolder(CalendarFolderEntity folder) {
        return planRepository.findByFolder(folder);
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
}

