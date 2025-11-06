package back.code.accountBook.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import back.code.accountBook.dto.AccountBookDTO;
import back.code.accountBook.service.AccountBookService;
import back.code.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/accountBook")
public class AccountBookController {

    private final AccountBookService accountBookService;

    
    // 가계부 작성
    @PostMapping("")
    public ResponseEntity<ApiResponse<AccountBookDTO.Detail>> writeAccount(
                                                        @ModelAttribute AccountBookDTO.Request request) throws Exception{
                                                            AccountBookDTO.Detail result = accountBookService.writeAccount(request);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
    
    // 가계부 수정
    @PutMapping("")
    public ResponseEntity<ApiResponse<AccountBookDTO.Detail>> updateAccount(
                                                        @ModelAttribute AccountBookDTO.Request request) throws Exception{
        AccountBookDTO.Detail result = accountBookService.updateAccount(request);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // 가계부 상세조회
    @GetMapping("")
    public ResponseEntity<ApiResponse<AccountBookDTO.Detail>> getAccount(
                                                        @RequestParam("userId") String userId,
                                                        @RequestParam("accountBookId") int accountBookId) throws Exception{
        AccountBookDTO.Detail result = accountBookService.getAccount(userId, accountBookId);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // 가계부 삭제
//    @PutMapping("/{accountId}")
//    public ResponseEntity<ApiResponse<AccountBookDTO.Detail>> deleteAccount(
//                                                        @PathVariable int AccountId) throws Exception{
//        AccountBookDTO.Detail result = accountBookService.deleteAccount(AccountId);
//        return ResponseEntity.ok(ApiResponse.ok(result));
//    }


}
