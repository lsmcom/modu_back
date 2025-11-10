package back.code.calendar.service;

import back.code.calendar.entity.PlanEntity;
import back.code.calendar.entity.PlanRepeatRuleEntity;
import back.code.calendar.repository.PlanRepeatRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PlanRepeatRuleService {

    private final PlanRepeatRuleRepository ruleRepository;

    /** 반복 규칙 등록 */
    public void saveRepeatRule(PlanEntity plan, String repeatType) {
        // 없음이면 저장 안 함
        if ("없음".equalsIgnoreCase(repeatType)) return;

        // 기존 규칙 존재 시 무시 (중복 방지)
        if (ruleRepository.existsByPlan_PlanId(plan.getPlanId())) return;

        PlanRepeatRuleEntity rule = PlanRepeatRuleEntity.builder()
                .plan(plan)
                .repeatType(repeatType)
                .build();

        ruleRepository.save(rule);
    }

    /** 반복 규칙 수정 (없음으로 변경 시 삭제) */
    public void updateRepeatRule(PlanEntity plan, String newRepeatType) {
        if ("없음".equalsIgnoreCase(newRepeatType)) {
            ruleRepository.deleteByPlan_PlanId(plan.getPlanId());
        } else {
            // 기존 규칙이 있으면 수정, 없으면 새로 등록
            ruleRepository.deleteByPlan_PlanId(plan.getPlanId());
            saveRepeatRule(plan, newRepeatType);
        }
    }

    /** 반복 규칙 삭제 */
    public void deleteRepeatRuleByPlan(Long planId) {
        ruleRepository.deleteByPlan_PlanId(planId);
    }
}
