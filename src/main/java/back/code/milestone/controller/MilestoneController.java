package back.code.milestone.controller;

import back.code.milestone.dto.UserMilestoneResponse;
import back.code.milestone.service.MilestoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/milestones")
public class MilestoneController {

    private final MilestoneService milestoneService;

    /**
     * 사용자가 달성한 모든 업적 목록을 조회합니다. (업적 페이지 메인)
     * GET /api/milestones
     * @param userId 요청 헤더에서 추출된 사용자 ID
     */
    @GetMapping
    public ResponseEntity<List<UserMilestoneResponse>> getUserMilestones(
            @RequestHeader("X-User-Id") String userId) {

        List<UserMilestoneResponse> milestones = milestoneService.getUserMilestonesByUserId(userId);
        return ResponseEntity.ok(milestones);
    }
}