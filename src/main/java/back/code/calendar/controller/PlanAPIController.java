package back.code.calendar.controller;

import back.code.calendar.dto.PlanCreateRequestDTO;
import back.code.calendar.dto.PlanResponse;
import back.code.calendar.dto.PlanUpdateRequestDTO;
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
    public ResponseEntity<PlanResponse> createPlan(@RequestBody PlanCreateRequestDTO request) {
        return ResponseEntity.ok(planService.createPlan(request));
    }

    /** 일정 수정 */
    @PutMapping("/{planId}")
    public ResponseEntity<PlanResponse> updatePlan(
            @PathVariable Long planId,
            @RequestBody PlanUpdateRequestDTO request) {

        PlanEntity updated = planService.updatePlan(planId, request);
        return ResponseEntity.ok(PlanResponse.fromEntity(updated));
    }


    /** 일정 삭제 */
    @DeleteMapping("/{planId}")
    public ResponseEntity<Void> deletePlan(@PathVariable Long planId) {
        planService.deletePlan(planId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PlanResponse>> getPlansByUser(@PathVariable String userId) {
        // 트랜지언트 객체 대신 레퍼런스/조회 권장
        UserEntity user = userRepository.getReferenceById(userId); // 또는 findById(...).orElseThrow(...)

        List<PlanResponse> plans = planService.getPlansByUser(user);
        plans.forEach(p ->
                System.out.println("[DEBUG] planId=" + p.getPlanId() + ", folderType=" + p.getFolderType())
        );
        return ResponseEntity.ok(plans);
    }

    /** 내 일정 + 공유받은 일정 */
    @GetMapping("/user/{userId}/all")
    public ResponseEntity<List<PlanResponse>> getPlansByUserIncludingShared(@PathVariable String userId) {
        List<PlanResponse> plans = planService.getPlansByUserIncludingShared(userId);
        return ResponseEntity.ok(plans);
    }

    /** 폴더별 일정 */
    @GetMapping("/folder/{folderId}")
    public ResponseEntity<List<PlanResponse>> getPlansByFolder(@PathVariable Long folderId) {
        List<PlanResponse> response = planService.getPlansByFolder(folderId);
        return ResponseEntity.ok(response);
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

    // 공유 일정 색상 변경
    @PutMapping("/shared/color")
    public ResponseEntity<String> updateSharedPlanColors(
            @RequestParam String userId,
            @RequestParam String color
    ) {
        planService.updateSharedPlanColors(userId, color);
        return ResponseEntity.ok("공유 일정 색상 변경 완료");
    }
}

