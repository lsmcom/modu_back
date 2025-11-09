package back.code.accountBook.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import back.code.accountBook.dto.AccountBookDTO;
import back.code.accountBook.dto.AccountSearchDTO;
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
                                            @RequestPart("request") AccountBookDTO.Request request,
                                            @RequestPart(value = "files", required = false) List<MultipartFile> files) throws Exception{
        AccountBookDTO.Detail result = accountBookService.writeAccount(request,files);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
    
    // 가계부 수정
    @PutMapping("")
    public ResponseEntity<ApiResponse<AccountBookDTO.Detail>> updateAccount(
                                            @RequestPart("request") AccountBookDTO.Request request,
                                            @RequestPart(value = "files", required = false) List<MultipartFile> files) throws Exception{
        AccountBookDTO.Detail result = accountBookService.updateAccount(request,files);
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
    @DeleteMapping("/{accountId}")
    public ResponseEntity<ApiResponse<AccountBookDTO.Detail>> deleteAccount(
                                            @RequestParam("userId") String userId,
                                            @PathVariable int accountId) throws Exception{
        AccountBookDTO.Detail result = accountBookService.deleteAccount(userId, accountId);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // 가계부 검색
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<List<AccountSearchDTO.AccountSearchResultDTO>>> searchAccountBook(
                                       @RequestBody AccountSearchDTO.Request search)throws Exception {

        List<AccountSearchDTO.AccountSearchResultDTO> result = accountBookService.searchAccountBook(search);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
