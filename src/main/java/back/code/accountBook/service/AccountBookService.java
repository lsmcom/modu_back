package back.code.accountBook.service;

import back.code.accountBook.entity.AccountFileMappingEntity;
import back.code.accountBook.repository.AccountFileMappingRepository;
import back.code.file.dto.FileDTO;
import back.code.file.service.FileService;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import back.code.accountBook.dto.AccountBookDTO;
import back.code.accountBook.entity.AccountBookEntity;
import back.code.accountBook.entity.AccountCategoryEntity;
import back.code.accountBook.entity.AccountSavingsGoalEntity;
import back.code.accountBook.repository.AccountBookRepository;
import back.code.accountBook.repository.CategoryRepository;
import back.code.accountBook.repository.SavingGoalRepository;
import back.code.user.entity.UserEntity;
import back.code.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AccountBookService {

    @Value("${server.file.upload.path}")
    private String filePath;

    private final UserRepository userRepository;
    private final AccountBookRepository accountBookRepository;
    private final CategoryRepository categoryRepository;
    private final SavingGoalRepository savingGoalRepository;
    private final FileService fileService;
    private final AccountFileMappingRepository mappingRepository;

    // 가계부 작성
    @Transactional
    public AccountBookDTO.Detail writeAccount(AccountBookDTO.Request request) throws Exception{

        // 사용자 확인
        UserEntity user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        // 카테고리 확인
        AccountCategoryEntity category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다."));
        // 저축 목표 확인
        AccountSavingsGoalEntity savingGoal = null;
        if (request.getSavingGoalId() != null) {
            savingGoal = savingGoalRepository.findById(request.getSavingGoalId())
                    .orElseThrow(() -> new RuntimeException("저축 목표를 찾을 수 없습니다."));
        }
        // DTO → 엔티티
        AccountBookEntity account = request.to(new AccountBookEntity(), user, category, savingGoal);
        accountBookRepository.save(account);
        // 파일 업로드
        if (request.getFiles() != null && !request.getFiles().isEmpty()) {
            for (MultipartFile multipartFile : request.getFiles()) {
                FileDTO fileDTO = fileService.uploadFile(multipartFile, user.getUserId(), "ACCOUNT");

                // 매핑 테이블 저장
                AccountFileMappingEntity mapping = new AccountFileMappingEntity();
                mapping.setAccount(account);
                mapping.setFile(fileService.getFileById(fileDTO.getFileId()));
                mappingRepository.save(mapping);

                account.getFiles().add(mapping);
            }
        }
        // DTO 변환
        AccountBookDTO.Detail detail = AccountBookDTO.Detail.of(account, filePath);

        return detail;
    }

    // 가계부 수정
    @Transactional
    public AccountBookDTO.Detail updateAccount(AccountBookDTO.Request request) throws Exception{

        // 사용자 확인
        UserEntity user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        // 기존 엔티티 조회
        AccountBookEntity account = accountBookRepository.findById(request.getAccountBookId())
                .orElseThrow(() -> new RuntimeException("수정할 가계부 항목을 찾을 수 없습니다."));
        // 카테고리 확인
        AccountCategoryEntity category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다."));
        // 저축 목표 확인
        AccountSavingsGoalEntity savingGoal = null;
        if (request.getSavingGoalId() != null) {
            savingGoal = savingGoalRepository.findById(request.getSavingGoalId())
                    .orElseThrow(() -> new RuntimeException("저축 목표를 찾을 수 없습니다."));
        }
        // 새 파일 업로드가 있는 경우
        if (request.getFiles() != null && !request.getFiles().isEmpty()) {
            // 기존 파일 정보 백업
            List<AccountFileMappingEntity> oldMappings = new ArrayList<>(account.getFiles());

            // 양방향관계 끊기
            for (AccountFileMappingEntity mapping : oldMappings) {
                mapping.setAccount(null);  // 관계 끊기
            }
            
            // 컬렉션 비우기
            account.getFiles().clear();
            accountBookRepository.flush();
            
            // 물리적 파일 삭제
            for (AccountFileMappingEntity mapping : oldMappings) {
                fileService.deleteFileEntity(mapping.getFile());
            }

            // 새 파일 업로드
            for (MultipartFile multipartFile : request.getFiles()) {
                FileDTO fileDTO = fileService.uploadFile(multipartFile, user.getUserId(), "ACCOUNT");

                AccountFileMappingEntity mapping = new AccountFileMappingEntity();
                mapping.setAccount(account);
                mapping.setFile(fileService.getFileById(fileDTO.getFileId()));
                
                account.getFiles().add(mapping);
            }
        }
        // DTO → 엔티티
        AccountBookEntity newAccount = request.to(account, user, category, savingGoal);
        // 저장
        AccountBookEntity savedAccount = accountBookRepository.save(newAccount);
        // DTO 변환
        AccountBookDTO.Detail detail = AccountBookDTO.Detail.of(savedAccount, filePath);

        return detail;
    }

    // 가계부 상세조회
    @Transactional
    public AccountBookDTO.Detail getAccount(String userId,int accountBookId) throws Exception {

        // 사용자 확인
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        // 기존 엔티티 조회
        AccountBookEntity account = accountBookRepository.findById(accountBookId)
                .orElseThrow(() -> new RuntimeException("해당 가계부 내역을 찾을 수 없습니다."));

        // DTO 변환
        AccountBookDTO.Detail detail = AccountBookDTO.Detail.of(account, filePath);

        return detail;
    }



    // 가계부 삭제
//    @Transactional
//    public AccountBookDTO.Detail deleteAccount(AccountBookDTO.Request request) throws Exception{
//
//    }

}
