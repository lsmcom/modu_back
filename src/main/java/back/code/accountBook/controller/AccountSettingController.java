package back.code.accountBook.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import back.code.accountBook.dto.AccountSavingGoalDTO;
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

}
