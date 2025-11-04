package back.code.accountBook.controller;

import back.code.accountBook.dto.AccountBookDTO;
import back.code.accountBook.service.AccountBookService;
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
public class AccountBookController {

    private final AccountBookService accountBookService;

    // 가계부 리스트(일별) 조회
    @GetMapping("/daily")
    public ResponseEntity<ApiResponse<List<AccountBookDTO.Response>>> dailyAccountList(
                                                                        @RequestParam String userId,
                                                                        @RequestParam LocalDate date)throws  Exception {
        List<AccountBookDTO.Response> result = accountBookService.getDailyAccountList(userId,date);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // 가계부 리스트(주별) 조회
    @GetMapping("/weekly")
    public ResponseEntity<ApiResponse<List<AccountBookDTO.WeekResponse>>> weeklyAccountList(
                                                                        @RequestParam String userId,
                                                                        @RequestParam LocalDate date)throws  Exception {
        List<AccountBookDTO.WeekResponse> result = accountBookService.getWeeklyAccountList(userId,date);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // 가계부 리스트(월별) 조회
   @GetMapping("/monthly")
   public ResponseEntity<ApiResponse<List<AccountBookDTO.MonthResponse>>> monthlyAccountList(
                                                                       @RequestParam String userId
                                                           )throws  Exception {
       List<AccountBookDTO.MonthResponse> result = accountBookService.getMonthlyAccountList(userId);
       return ResponseEntity.ok(ApiResponse.ok(result));
   }

    // 가계부 리스트(달력) 조회
    @GetMapping("/calendar")
    public ResponseEntity<ApiResponse<AccountBookDTO.CalendarAccountResponse>> calendarAccountList(
                                                                     @RequestParam String userId,
                                                                     @RequestParam LocalDate date)throws  Exception {
        AccountBookDTO.CalendarAccountResponse result =
                                    accountBookService.getCalendarAccountList(userId,date);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }




}
