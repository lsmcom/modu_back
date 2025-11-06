package back.code.accountBook.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import back.code.accountBook.dto.AccountCategoryDTO;
import back.code.accountBook.entity.AccountCategoryEntity;
import back.code.accountBook.service.AccountCategoryService;
import back.code.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/accountBook")
public class AccountCategoryController {
    
    private final AccountCategoryService accountCategoryService;

     // 카테고리 조회
    @GetMapping("/category")
    public ResponseEntity<ApiResponse<List<AccountCategoryEntity>>> categoryList(
                                                            @RequestParam("userId") String userId) throws Exception{
        List<AccountCategoryEntity> result = accountCategoryService.categoryList(userId);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // 카테고리 추가
    @PostMapping("/category")
    public ResponseEntity<ApiResponse<AccountCategoryEntity>> categoryAdd(
                                                    @RequestBody AccountCategoryDTO.Request request) throws Exception{
        AccountCategoryEntity result = accountCategoryService.categoryAdd(request);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

   // 카테고리 수정
    @PutMapping("/category")
    public ResponseEntity<ApiResponse<AccountCategoryEntity>> categoryUpdate(
                                                    @RequestBody AccountCategoryDTO.Request request) throws Exception{
        AccountCategoryEntity result = accountCategoryService.categoryUpdate(request);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // 카테고리삭제
    @DeleteMapping("/category")
    public ResponseEntity<ApiResponse<AccountCategoryDTO.Response>> categoryDelete(
                                                            @RequestParam("userId") String userId,
                                                            @RequestParam("categoryId")int categoryId) throws Exception{
        AccountCategoryDTO.Response result = accountCategoryService.categoryDelete(userId, categoryId);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

}
