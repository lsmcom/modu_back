package back.code.accountBook.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import back.code.accountBook.dto.AccountBookDTO;
import back.code.accountBook.entity.AccountBookEntity;
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
    @PostMapping("/write")
    public ResponseEntity<ApiResponse<AccountBookEntity>> writeAccount(
                                                        @RequestBody AccountBookDTO.Request request) throws Exception{
        AccountBookEntity result = accountBookService.writeAccount(request);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }


}
