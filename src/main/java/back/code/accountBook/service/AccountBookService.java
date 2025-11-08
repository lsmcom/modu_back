package back.code.accountBook.service;

import back.code.accountBook.entity.AccountFileMappingEntity;
import back.code.accountBook.repository.AccountFileMappingRepository;
import back.code.file.dto.FileDTO;
import back.code.file.entity.FileEntity;
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
        AccountSavingsGoalEntity savingGoal = account.getGoal();
        if (request.getSavingGoalId() != null) {
            savingGoal = savingGoalRepository.findById(request.getSavingGoalId())
                    .orElseThrow(() -> new RuntimeException("저축 목표를 찾을 수 없습니다."));
        }
        // 현재 파일 매핑 복사
        List<AccountFileMappingEntity> currentMappings = new ArrayList<>(account.getFiles());
        // 유지파일, 삭제파일 분리
        List<String> existingFileIds = request.getExistingFileIds();
        List<AccountFileMappingEntity> toKeep = new ArrayList<>();
        List<AccountFileMappingEntity> toDelete = new ArrayList<>();

        for (AccountFileMappingEntity mapping : currentMappings) {
            if (existingFileIds != null && existingFileIds.contains(mapping.getFile().getFileId())) {
                toKeep.add(mapping);  // 유지
            } else {
                toDelete.add(mapping); // 삭제
            }
        }
        // 삭제할 파일 DTO 리스트로 만들기 (물리파일 삭제용)
        List<FileDTO> filesToDelete = new ArrayList<>();
        for (AccountFileMappingEntity mapping : toDelete) {
            filesToDelete.add(FileDTO.from(mapping.getFile(), filePath));
        }
        // 매핑에서 제거
        account.getFiles().removeAll(toDelete);
        for (AccountFileMappingEntity mapping : toDelete) {
            mapping.setAccount(null); // 참조끊기
        }
        // 새 파일 추가
        if (request.getFiles() != null && !request.getFiles().isEmpty()) {
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
        // 물리파일 삭제
        for (FileDTO fileDTO : filesToDelete) {
            try {
                FileEntity fileEntity = fileService.getFileById(fileDTO.getFileId());
                fileService.deleteFileEntity(fileEntity);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
    @Transactional
    public AccountBookDTO.Detail deleteAccount(String userId, int accountBookId) throws Exception{

        // 기존 엔티티 조회
        AccountBookEntity account = accountBookRepository.findById(accountBookId)
                .orElseThrow(() -> new RuntimeException("해당 가계부 내역을 찾을 수 없습니다."));
        // 해당 가계부 사용자의 것인지 확인
        if(!account.getUser().getUserId().equals(userId)){
            throw new IllegalArgumentException("해당 가계부에 대한 권한이 없습니다.");
        }
        // dto 변경
        AccountBookDTO.Detail detail = AccountBookDTO.Detail.of(account, filePath);
        // FileDB삭제, 물리적 삭제
        for (AccountFileMappingEntity mapping : account.getFiles()) {
            FileEntity file = mapping.getFile();
            if (file != null) {
                mappingRepository.delete(mapping);  // 매핑 삭제
                fileService.deleteFileEntity(file);  // 파일 삭제
            }
        }
        // 삭제
        accountBookRepository.delete(account);

        return detail;
    }

}
