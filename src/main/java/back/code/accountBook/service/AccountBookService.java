package back.code.accountBook.service;

import back.code.accountBook.entity.AccountFileMappingEntity;
import back.code.accountBook.enums.AccountType;
import back.code.accountBook.repository.AccountFileMappingRepository;
import back.code.accountBook.repository.AccountSearchRepository;
import back.code.accountBook.repository.AccountSearchSpecification;
import back.code.file.dto.FileDTO;
import back.code.file.entity.FileEntity;
import back.code.file.service.FileService;
import back.code.recentsearch.service.RecentSearchService;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import back.code.accountBook.dto.AccountBookDTO;
import back.code.accountBook.dto.AccountSearchDTO;
import back.code.accountBook.entity.AccountBookEntity;
import back.code.accountBook.entity.AccountCategoryEntity;
import back.code.accountBook.entity.AccountSavingsGoalEntity;
import back.code.accountBook.entity.InstallmentSettingEntity;
import back.code.accountBook.entity.RecurringSettingEntity;
import back.code.accountBook.repository.AccountBookRepository;
import back.code.accountBook.repository.CategoryRepository;
import back.code.accountBook.repository.InstallmentSettingRepository;
import back.code.accountBook.repository.RecurringSettingRepository;
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
    private final RecurringSettingRepository recurringRepository;
    private final InstallmentSettingRepository installmentRepository;
    private final AccountSearchRepository searchRepository;
    private final RecentSearchService recentSearchService;
    private static final String SEARCH_TYPE = "ACCOUNT";

    // 가계부 작성
    @Transactional
    public AccountBookDTO.Detail writeAccount(AccountBookDTO.Request request,List<MultipartFile> files) throws Exception{
        
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
            if (AccountType.INCOME.equals(request.getType())) {
                int currentAmount = savingGoal.getCurrentAmount() != null ? savingGoal.getCurrentAmount() : 0;
                savingGoal.setCurrentAmount(currentAmount + request.getAmount());
                savingGoalRepository.save(savingGoal);
            }
        }
        // DTO → 엔티티
        AccountBookEntity account = request.to(new AccountBookEntity(), user, category, savingGoal);
        accountBookRepository.save(account);

        // 반복설정 저장
        if (request.getRecurring() != null) {
            for (AccountBookDTO.RecurringDTO recurringDTO : request.getRecurring()) {

                // 기존 엔티티 찾거나 새로 생성
                RecurringSettingEntity recurringEntity = recurringRepository.findByAccount(account)
                    .orElseGet(() -> {
                        RecurringSettingEntity r = new RecurringSettingEntity();
                        r.setAccount(account);
                        return r;
                    });

                // 작성일을 반복 시작일로 설정
                recurringDTO.setStartDate(account.getDate());

                // 다음 반복일 계산
                LocalDate nextDate = calculateNextRecurringDate(recurringDTO.getStartDate(), recurringDTO);
                recurringDTO.setNextDate(nextDate);

                // DTO 값을 기존 Entity에 적용
                recurringDTO.to(recurringEntity);

                // 저장
                recurringRepository.save(recurringEntity);
            }
        }

        // 할부설정 저장
        if (request.getInstallment() != null) {
            for (AccountBookDTO.InstallmentDTO installmentDTO : request.getInstallment()) {
                if (installmentDTO.getStartDate() == null) {
                    installmentDTO.setStartDate(account.getDate());
                }

                InstallmentSettingEntity installmentEntity = new InstallmentSettingEntity();
                installmentEntity.setAccount(account);
                installmentDTO.to(installmentEntity);
                installmentRepository.save(installmentEntity);
            }
        }

        // 파일 업로드
        if (files != null && !files.isEmpty()) {
            for (MultipartFile multipartFile : files) {
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
    public AccountBookDTO.Detail updateAccount(AccountBookDTO.Request request, List<MultipartFile> files) throws Exception{

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
        AccountSavingsGoalEntity oldGoal = account.getGoal();  // 기존 저축 목표
        Integer oldAmount = account.getAmount();  // 기존 금액

        // 기존 저축 목표가 있고 수입이었다면 기존 금액 차감
        if (oldGoal != null && AccountType.INCOME.equals(account.getType())) {
            int currentAmount = oldGoal.getCurrentAmount() != null ? oldGoal.getCurrentAmount() : 0;
            oldGoal.setCurrentAmount(currentAmount - oldAmount);
            savingGoalRepository.save(oldGoal);
        }

        // 새로운 저축 목표 처리
        AccountSavingsGoalEntity savingGoal = null;
        if (request.getSavingGoalId() != null) {
            savingGoal = savingGoalRepository.findById(request.getSavingGoalId())
                    .orElseThrow(() -> new RuntimeException("저축 목표를 찾을 수 없습니다."));

            // 수입일 때만 새로운 금액 추가
            if (AccountType.INCOME.equals(request.getType())) {
                int currentAmount = savingGoal.getCurrentAmount() != null ? savingGoal.getCurrentAmount() : 0;
                savingGoal.setCurrentAmount(currentAmount + request.getAmount());
                savingGoalRepository.save(savingGoal);
            }
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
        if (files != null && !files.isEmpty()) {
            for (MultipartFile multipartFile : files) {
                FileDTO fileDTO = fileService.uploadFile(multipartFile, user.getUserId(), "ACCOUNT");

                AccountFileMappingEntity mapping = new AccountFileMappingEntity();
                mapping.setAccount(account);
                mapping.setFile(fileService.getFileById(fileDTO.getFileId()));

                account.getFiles().add(mapping);
            }
        }
        // DTO → 엔티티
        AccountBookEntity newAccount = request.to(account, user, category, savingGoal);

        // 반복 설정
        if (request.getRecurring() != null) {
            for (AccountBookDTO.RecurringDTO recurringDTO : request.getRecurring()) {
                RecurringSettingEntity recurring = recurringRepository.findByAccount(account)
                    .orElseGet(() -> {
                        RecurringSettingEntity r = recurringDTO.to(new RecurringSettingEntity());
                        r.setAccount(account);
                        return recurringRepository.save(r);
                    });

                // DTO -> Entity
                recurringDTO.to(recurring);
                // 반복종료 체크
                checkRecurring(recurring);

                recurringRepository.save(recurring);
            }
        }

        // 할부 설정 처리
        if (request.getInstallment() != null) {
            for (AccountBookDTO.InstallmentDTO installmentDTO : request.getInstallment()) {
                InstallmentSettingEntity installment = installmentRepository.findByAccount(account)
                    .orElseGet(() -> {
                        InstallmentSettingEntity i = installmentDTO.to(new InstallmentSettingEntity());
                        i.setAccount(account);
                        return installmentRepository.save(i);
                    });

                // DTO -> Entity
                installmentDTO.to(installment);
                installmentRepository.save(installment);
            }
        }

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

        // 반복설정/할부설정 존재하면 삭제
        recurringRepository.findByAccount(account).ifPresent(recurringRepository::delete);
        installmentRepository.findByAccount(account).ifPresent(installmentRepository::delete);

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

    // 가계부 검색
    @Transactional
    public List<AccountSearchDTO.AccountSearchResultDTO> searchAccountBook(AccountSearchDTO.Request search) {
        
        // 검색어가 있으면 최근 검색어에 저장
        if (search.getKeyword() != null && !search.getKeyword().trim().isEmpty()) {
            recentSearchService.saveRecentSearch(
                search.getUserId(), 
                SEARCH_TYPE, 
                search.getKeyword().trim()
            );
        }

        Specification<AccountBookEntity> spec = new AccountSearchSpecification(search);

        // 검색 실행
        List<AccountBookEntity> result = searchRepository.findAll(spec);

        // DTO 변환
        List<AccountSearchDTO.AccountSearchResultDTO> searchResult = result.stream()
                                        .map(AccountSearchDTO.AccountSearchResultDTO::of)
                                        .collect(Collectors.toList());

        return searchResult;
    }

    // 다음 반복일 계산
    private LocalDate calculateNextRecurringDate(LocalDate startDate, AccountBookDTO.RecurringDTO recurring) {
        String cycle = recurring.getCycle().name();
        LocalDate nextDate = startDate;
        // 주기별 계산
        switch(cycle) {
            // 매일
            case "DAILY":
                nextDate = startDate.plusDays(1);
                break;
            // 매주
            case "WEEKLY":
                if (recurring.getDaysOfWeek() == null || recurring.getDaysOfWeek().isEmpty()) {
                    throw new RuntimeException("요일을 선택해주세요.");
                }
                List<Integer> weekDays = Arrays.stream(recurring.getDaysOfWeek().split(","))
                        .map(Integer::parseInt)
                        .collect(Collectors.toList());
                int today = startDate.getDayOfWeek().getValue(); // 1=월, 7=일
                int diff = weekDays.stream()
                        .map(d -> (d - today + 7) % 7)
                        .filter(d -> d != 0) // 0은 오늘이므로 제외
                        .min(Integer::compareTo) // 최소값 : 가장 가까운 다음 반복일
                        .orElse(7);
                nextDate = startDate.plusDays(diff);
                break;
            // 매월    
            case "MONTHLY":
                int dayOfMonth = startDate.getDayOfMonth();
                nextDate = startDate.withDayOfMonth(Math.min(dayOfMonth, startDate.lengthOfMonth()));
                if (!nextDate.isAfter(startDate)) nextDate = nextDate.plusMonths(1);
                break;
            // 매년    
            case "YEARLY":
                int month = startDate.getMonthValue();
                int day = startDate.getDayOfMonth();
                nextDate = LocalDate.of(startDate.getYear(), month, Math.min(day, YearMonth.of(startDate.getYear(), month).lengthOfMonth()));
                if (!nextDate.isAfter(startDate)) nextDate = nextDate.plusYears(1);
                break;
        }

        return nextDate;
    }

    // 반복 종료
    @Transactional
    public void checkRecurring(RecurringSettingEntity recurring) {
        if (recurring.getEndDate() != null && LocalDate.now().isAfter(recurring.getEndDate())) {
            recurring.setIsActive(false);
            recurringRepository.save(recurring);
        }
    }

}
