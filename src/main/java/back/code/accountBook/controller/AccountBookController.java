package back.code.accountBook.controller;

import back.code.file.dto.FileDTO;
import back.code.file.entity.FileEntity;
import back.code.file.repository.FileRepository;
import back.code.file.service.FileService;
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
    private final FileService fileService;
    private final FileRepository fileRepository;

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
    @DeleteMapping("/{accountId}")
    public ResponseEntity<ApiResponse<AccountBookDTO.Detail>> deleteAccount(
                                                        @RequestParam("userId") String userId,
                                                        @PathVariable int accountId) throws Exception{
        AccountBookDTO.Detail result = accountBookService.deleteAccount(userId, accountId);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // 파일 삭제
//    @DeleteMapping("/{fileId}")
//    public ResponseEntity<Void>> deleteFile(@PathVariable int fileId) throws Exception {
//        FileEntity file = fileRepository.findById(fileId)
//                .orElseThrow(() -> new RuntimeException("파일을 찾을 수 없습니다."));
//
//        FileDTO.FileDTOBuilder result = fileService.deleteFileEntity(file);
//
//        return ResponseEntity.ok(ApiResponse.ok(result));
//    }


}
