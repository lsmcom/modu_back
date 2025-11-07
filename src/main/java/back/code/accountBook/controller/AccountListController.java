package back.code.accountBook.controller;

import back.code.accountBook.dto.AccountBookDTO;
import back.code.accountBook.service.AccountListService;
import back.code.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/accountBook")
public class AccountListController {

    private final AccountListService accountListService;

    // 가계부 리스트(일별) 조회
    @GetMapping("/daily")
    public ResponseEntity<ApiResponse<List<AccountBookDTO.Response>>> dailyAccountList(
                                                                        @RequestParam("userId") String userId,
                                                                        @RequestParam("date") LocalDate date)throws  Exception {
        List<AccountBookDTO.Response> result = accountListService.getDailyAccountList(userId,date);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // 가계부 리스트(주별) 조회
    @GetMapping("/weekly")
    public ResponseEntity<ApiResponse<List<AccountBookDTO.WeekResponse>>> weeklyAccountList(
                                                                        @RequestParam("userId") String userId,
                                                                        @RequestParam("date") LocalDate date)throws  Exception {
        List<AccountBookDTO.WeekResponse> result = accountListService.getWeeklyAccountList(userId,date);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // 가계부 리스트(월별) 조회
   @GetMapping("/monthly")
   public ResponseEntity<ApiResponse<List<AccountBookDTO.MonthResponse>>> monthlyAccountList(
                                                                       @RequestParam("userId") String userId
                                                           )throws  Exception {
       List<AccountBookDTO.MonthResponse> result = accountListService.getMonthlyAccountList(userId);
       return ResponseEntity.ok(ApiResponse.ok(result));
   }

    // 가계부 리스트(달력) 조회
    @GetMapping("/calendar")
    public ResponseEntity<ApiResponse<AccountBookDTO.CalendarAccountResponse>> calendarAccountList(
                                                                     @RequestParam("userId") String userId,
                                                                     @RequestParam("date") LocalDate date)throws  Exception {
        AccountBookDTO.CalendarAccountResponse result =
                                    accountListService.getCalendarAccountList(userId,date);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

}
