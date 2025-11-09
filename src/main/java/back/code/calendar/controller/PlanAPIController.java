package back.code.calendar.controller;

import back.code.calendar.dto.PlanResponse;
import back.code.calendar.entity.CalendarFolderEntity;
import back.code.calendar.entity.PlanEntity;
import back.code.calendar.entity.PlanShareEntity;
import back.code.calendar.repository.CalendarFolderRepository;
import back.code.calendar.service.PlanService;
import back.code.user.entity.UserEntity;
import back.code.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/calendar/plan")
@RequiredArgsConstructor
public class PlanAPIController {

    private final PlanService planService;
    private final CalendarFolderRepository calendarFolderRepository;
    private final UserRepository userRepository;

    /** 일정 등록 */
    @PostMapping
    public ResponseEntity<PlanEntity> createPlan(@RequestBody PlanEntity plan) {
        // folderId 유효성 확인 및 실제 엔티티로 교체
        if (plan.getFolder() != null && plan.getFolder().getFolderId() != null) {
            Long folderId = plan.getFolder().getFolderId();
            CalendarFolderEntity folder = calendarFolderRepository.findById(folderId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 폴더 ID: " + folderId));
            plan.setFolder(folder);
        } else {
            throw new IllegalArgumentException("폴더 ID가 누락되었습니다.");
        }

        // userId 유효성 확인 및 실제 엔티티로 교체
        if (plan.getUser() != null && plan.getUser().getUserId() != null) {
            String userId = plan.getUser().getUserId();
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자 ID: " + userId));
            plan.setUser(user);
        } else {
            throw new IllegalArgumentException("사용자 ID가 누락되었습니다.");
        }

        // Plan 저장
        PlanEntity savedPlan = planService.createPlan(plan);
        return ResponseEntity.ok(savedPlan);
    }

    /** 일정 수정 */
    @PutMapping("/{planId}")
    public ResponseEntity<PlanEntity> updatePlan(
            @PathVariable String planId, @RequestBody PlanEntity updated) {
        return ResponseEntity.ok(planService.updatePlan(planId, updated));
    }

    /** 일정 삭제 */
    @DeleteMapping("/{planId}")
    public ResponseEntity<Void> deletePlan(@PathVariable String planId) {
        planService.deletePlan(planId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PlanResponse>> getPlansByUser(@PathVariable String userId) {
        UserEntity user = new UserEntity();
        user.setUserId(userId);
        List<PlanResponse> plans = planService.getPlansByUser(user)
                .stream().map(PlanResponse::fromEntity).toList();
        return ResponseEntity.ok(plans);
    }

    /** 폴더별 일정 */
    @GetMapping("/folder/{folderId}")
    public ResponseEntity<List<PlanEntity>> getPlansByFolder(@PathVariable Long folderId) {
        CalendarFolderEntity folder = new CalendarFolderEntity();
        folder.setFolderId(folderId);
        return ResponseEntity.ok(planService.getPlansByFolder(folder));
    }

    /** 일정 공유자 목록 */
    @GetMapping("/{planId}/share")
    public ResponseEntity<List<PlanShareEntity>> getSharedUsers(@PathVariable Long planId) {
        PlanEntity plan = new PlanEntity();
        plan.setPlanId(planId);
        return ResponseEntity.ok(planService.getSharedUsers(plan));
    }

    /** 일정 공유자 추가 */
    @PostMapping("/{planId}/share/{sharedUserId}")
    public ResponseEntity<Void> addSharedUser(@PathVariable Long planId, @PathVariable String sharedUserId) {
        PlanEntity plan = new PlanEntity();
        plan.setPlanId(planId);
        UserEntity user = new UserEntity();
        user.setUserId(sharedUserId);
        planService.addSharedUser(plan, user);
        return ResponseEntity.ok().build();
    }

    /** 일정 공유자 삭제 */
    @DeleteMapping("/{planId}/share/{sharedUserId}")
    public ResponseEntity<Void> removeSharedUser(@PathVariable Long planId, @PathVariable String sharedUserId) {
        PlanEntity plan = new PlanEntity();
        plan.setPlanId(planId);
        UserEntity user = new UserEntity();
        user.setUserId(sharedUserId);
        planService.removeSharedUser(plan, user);
        return ResponseEntity.noContent().build();
    }
}

