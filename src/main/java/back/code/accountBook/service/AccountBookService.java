package back.code.accountBook.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import back.code.accountBook.entity.AccountFileMappingEntity;
import back.code.accountBook.repository.AccountFileMappingRepository;
import back.code.common.utils.FileUtils;
import back.code.file.dto.FileDTO;
import back.code.file.entity.FileEntity;
import back.code.file.repository.FileRepository;
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
    private final FileUtils fileUtils;
    private final FileRepository fileRepository;
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

        if (request.getFiles() != null && !request.getFiles().isEmpty()) {
            for (MultipartFile multipartFile : request.getFiles()) {
                Map<String, Object> uploaded = fileUtils.uploadFile(multipartFile, filePath, "ACCOUNT");

                FileEntity fileEntity = new FileEntity();
                fileEntity.setFileId(UUID.randomUUID().toString());
                fileEntity.setUser(user);
                fileEntity.setFileType("ACCOUNT");
                fileEntity.setFileName((String) uploaded.get("originalName"));
                fileEntity.setStoredName((String) uploaded.get("storedFileName"));
                fileEntity.setFilePath((String) uploaded.get("filePath"));
                fileEntity.setFileSize((Long) uploaded.get("fileSize"));
                fileRepository.save(fileEntity);

                AccountFileMappingEntity mapping = new AccountFileMappingEntity();
                mapping.setAccount(account);
                mapping.setFile(fileEntity);
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
        // 새 파일 업로드
        if (request.getFiles() != null && !request.getFiles().isEmpty()) {
            for (MultipartFile multipartFile : request.getFiles()) {
                Map<String, Object> uploaded = fileUtils.uploadFile(multipartFile, filePath, "ACCOUNT");

                FileEntity fileEntity = new FileEntity();
                fileEntity.setFileId(UUID.randomUUID().toString());
                fileEntity.setUser(user);
                fileEntity.setFileType("ACCOUNT");
                fileEntity.setFileName((String) uploaded.get("originalName"));
                fileEntity.setStoredName((String) uploaded.get("storedFileName"));
                fileEntity.setFilePath((String) uploaded.get("filePath"));
                fileEntity.setFileSize((Long) uploaded.get("fileSize"));
                fileRepository.save(fileEntity);

                AccountFileMappingEntity mapping = new AccountFileMappingEntity();
                mapping.setAccount(account);
                mapping.setFile(fileEntity);
                mappingRepository.save(mapping);

                account.getFiles().add(mapping);
            }
        }
        // DTO → 엔티티
        AccountBookEntity newAccount = request.to(account, user, category, savingGoal);
        // 저장
        AccountBookEntity savedAccount = accountBookRepository.save(newAccount);
        // DTO 변환
        AccountBookDTO.Detail detail = AccountBookDTO.Detail.of(savedAccount, filePath);
        // 기존 파일 삭제
        if (account.getFiles() != null && !account.getFiles().isEmpty()) {
            for (AccountFileMappingEntity mapping : account.getFiles()) {
                FileEntity oldFile = mapping.getFile();
                String oldFilePath = filePath + oldFile.getStoredName();
                fileUtils.deleteFile(oldFilePath); // 서버 물리 파일 삭제
                fileRepository.delete(oldFile);     // DB FileEntity 삭제
            }
            mappingRepository.deleteAll(account.getFiles()); // 매핑 테이블 삭제
            account.getFiles().clear();
        }

        return detail;
    }

    // 가계부 삭제
//    @Transactional
//    public AccountBookDTO.Detail deleteAccount(AccountBookDTO.Request request) throws Exception{
//
//    }

}
