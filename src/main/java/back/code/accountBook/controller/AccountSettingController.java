package back.code.accountBook.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import back.code.accountBook.dto.AccountSavingGoalDTO;
import back.code.accountBook.dto.BudgetDTO;
import back.code.accountBook.service.AccountSettingService;
import back.code.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/accountBook")
public class AccountSettingController {

    private final AccountSettingService accountSettingService;

    // 작성창용 저축목표
    @GetMapping("/goal")
    public ResponseEntity<ApiResponse<List<AccountSavingGoalDTO.writeGoals>>> getWriteGoals(
                                                    @RequestParam("userId") String userId) throws Exception{
        List<AccountSavingGoalDTO.writeGoals> result = accountSettingService.getWriteGoals(userId);

        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // 월별 예산 조회
    @GetMapping("/budget")
    public ResponseEntity<ApiResponse<BudgetDTO.BudgetSettingResponse>> getBudgetSetting(
                                                                @RequestParam String userId,
                                                                @RequestParam String yearMonth) throws Exception {
        BudgetDTO.BudgetSettingResponse result = accountSettingService.getBudgetSetting(userId, yearMonth);
        
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // 전체 예산 저장
    @PostMapping("/budget/total")
    public ResponseEntity<ApiResponse<String>> saveTotalBudget(@RequestBody BudgetDTO.TotalBudgetRequest request) {
        accountSettingService.saveTotalBudget(request.getUserId(), request.getYearMonth(), request.getTotalBudget());
        return ResponseEntity.ok(ApiResponse.ok("전체 예산이 저장되었습니다."));
    }

    // 카테고리별 예산 저장
    @PostMapping("/budget/category")
    public ResponseEntity<ApiResponse<String>> saveCategoryBudgets(@RequestBody BudgetDTO.BudgetSettingRequest request) {
        accountSettingService.saveCategoryBudgets(request.getUserId(), request.getYearMonth(), request.getCategoryBudgets());
        return ResponseEntity.ok(ApiResponse.ok("카테고리별 예산이 저장되었습니다."));
    }








}
