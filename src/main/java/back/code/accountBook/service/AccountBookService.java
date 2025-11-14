package back.code.accountBook.service;

import back.code.accountBook.entity.AccountFileMappingEntity;
import back.code.accountBook.enums.AccountType;
import back.code.accountBook.repository.AccountFileMappingRepository;
import back.code.accountBook.repository.AccountSearchRepository;
import back.code.accountBook.repository.AccountSearchSpecification;
import back.code.common.utils.FileUtils;
import back.code.file.dto.FileDTO;
import back.code.file.entity.FileEntity;
import back.code.file.repository.FileRepository;
import back.code.file.service.FileService;
import back.code.milestone.repository.MilestoneRepository;
import back.code.milestone.service.MilestoneService;
import back.code.recentsearch.service.RecentSearchService;

import java.nio.file.Paths;
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
    
    private static final String SEARCH_TYPE = "ACCOUNT";
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
    private final FileRepository fileRepository;
    private final FileUtils fileUtils;
    private final MilestoneService milestoneService;


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
        RecurringSettingEntity recurring = null;
        if (request.getRecurring() != null) {
            AccountBookDTO.RecurringDTO recurringDTO = request.getRecurring();

            recurring = new RecurringSettingEntity();
            recurring.setAccount(account);
            recurring.setIsActive(true);

            // 반복 시작일 설정
            recurringDTO.setStartDate(account.getDate());

            // 다음 반복일 계산
            LocalDate nextDate = calculateNextRecurringDate(recurringDTO.getStartDate(), recurringDTO);
            recurringDTO.setNextDate(nextDate);

            // DTO 값 엔티티에 적용
            recurringDTO.to(recurring);

            // 저장
            recurringRepository.save(recurring);
        }

        // 할부설정 저장
        InstallmentSettingEntity installment = null;
        if (request.getInstallment() != null) {
            AccountBookDTO.InstallmentDTO installmentDTO = request.getInstallment();

            if (installmentDTO.getStartDate() == null) {
                installmentDTO.setStartDate(account.getDate());
            }

            installment = new InstallmentSettingEntity();
            installment.setAccount(account);
            installmentDTO.to(installment);
            installmentRepository.save(installment);
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
        AccountBookDTO.Detail detail = AccountBookDTO.Detail.of(account, filePath, recurring, installment);

        milestoneService.checkAndAwardMilestones(user.getUserId());

        return detail;
    }

    // 가계부 수정
    @Transactional
    public AccountBookDTO.Detail updateAccount(AccountBookDTO.Request request, List<MultipartFile> files) throws Exception{

        // 사용자 확인
        UserEntity user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        // 기존 엔티티 조회
        AccountBookEntity account = accountBookRepository.findById(request.getAccountId())
                .orElseThrow(() -> new RuntimeException("수정할 가계부 항목을 찾을 수 없습니다."));
        // 카테고리 확인
        AccountCategoryEntity category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다."));

        // 저축 목표 확인
        AccountSavingsGoalEntity oldGoal = account.getGoal();  // 기존 저축 목표
        Integer oldAmount = account.getAmount();  // 기존 금액
        // 새로운 목표와 금액
        Integer newAmount = request.getAmount();
        Integer newGoalId = request.getSavingGoalId();
        AccountType newType = request.getType();

        AccountSavingsGoalEntity savingGoal = null;

        // 기존 목표가 있고, 수입인 경우
        if (oldGoal != null && AccountType.INCOME.equals(account.getType())) {
            int currentAmount = oldGoal.getCurrentAmount() != null ? oldGoal.getCurrentAmount() : 0;

            if (oldGoal.getGoalId().equals(newGoalId)) {
                // 목표가 동일하면 금액 차이만 계산
                int diff = (newAmount != null ? newAmount : 0) - (oldAmount != null ? oldAmount : 0);

                if (diff != 0) {  // 금액 차이가 있을 때만 save
                    oldGoal.setCurrentAmount(currentAmount + diff);
                    savingGoalRepository.save(oldGoal);
                }

                savingGoal = oldGoal; 

            } else {
                // 목표가 바뀌었으면 기존 목표에서 기존 금액만 차감
                if (oldAmount != null && oldAmount > 0) {  // 차감할 금액이 있을 때만
                    oldGoal.setCurrentAmount(currentAmount - oldAmount);
                    savingGoalRepository.save(oldGoal);
                }
            }
        }
        // 새로운 목표 처리
        if (newGoalId != null) {
            savingGoal = savingGoalRepository.findById(newGoalId)
                    .orElseThrow(() -> new RuntimeException("저축 목표를 찾을 수 없습니다."));

            if (AccountType.INCOME.equals(newType) && 
                (oldGoal == null || !oldGoal.getGoalId().equals(newGoalId))) {
                int currentAmount = savingGoal.getCurrentAmount() != null ? savingGoal.getCurrentAmount() : 0;
                savingGoal.setCurrentAmount(currentAmount + newAmount);
                savingGoalRepository.save(savingGoal);
            }
        }

        // 반복 설정
        RecurringSettingEntity recurring = null;
        if (request.getRecurring() != null) {
            AccountBookDTO.RecurringDTO recurringDTO = request.getRecurring();

            // 기존 엔티티 찾거나 새로 생성
            recurring = recurringRepository.findByAccount(account)
                .orElseGet(() -> {
                    RecurringSettingEntity r = new RecurringSettingEntity();
                    r.setAccount(account);
                    r.setIsActive(true);
                    return r;
                });

            // 반복 시작일을 현재 계정 날짜로 설정
            recurringDTO.setStartDate(account.getDate());

            // 다음 반복일 계산
            LocalDate nextDate = calculateNextRecurringDate(recurringDTO.getStartDate(), recurringDTO);
            recurringDTO.setNextDate(nextDate);

            // DTO 값을 기존 Entity에 적용
            recurringDTO.to(recurring);

            // 반복 종료 체크
            checkRecurring(recurring);

            // 저장
            recurringRepository.save(recurring);
        }

        // 할부 설정 처리
        InstallmentSettingEntity installment = null;
        if (request.getInstallment() != null) {
            AccountBookDTO.InstallmentDTO installmentDTO = request.getInstallment();

            // 기존 엔티티 조회, 없으면 새로 생성
            installment = installmentRepository.findByAccount(account)
                .orElseGet(() -> {
                    InstallmentSettingEntity i = new InstallmentSettingEntity();
                    i.setAccount(account);
                    return i;
                });

            // 시작일 기본값 설정
            if (installmentDTO.getStartDate() == null) {
                installmentDTO.setStartDate(account.getDate());
            }

            // DTO → Entity 적용
            installmentDTO.to(installment);

            // 저장
            installmentRepository.save(installment);
        }

        // 삭제할 파일 ID
        List<String> existingFileIds = request.getExistingFileIds();
        if (existingFileIds == null) {
            existingFileIds = new ArrayList<>();  // null이면 빈 리스트로 초기화
        }

        List<String> toDeleteIds = account.getFiles().stream()
                .map(fm -> fm.getFile().getFileId())
                .filter(id -> !request.getExistingFileIds().contains(id))
                .toList();

        // DB/물리 파일 삭제
        for(String fileId : toDeleteIds) {
            deleteFile(account.getAccountId(), fileId);
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

        // 저장
        AccountBookEntity savedAccount = accountBookRepository.save(account);
        
        // DTO 변환
        AccountBookDTO.Detail detail = AccountBookDTO.Detail.of(savedAccount, filePath, recurring, installment);

        return detail;
    }

    // 파일 삭제
    @Transactional
    public void deleteFile(Integer accountId, String fileId) throws Exception{
 
        // 삭제 대상 파일 조회
        var mappingOpt = mappingRepository.findByAccount_AccountIdAndFile_FileId(accountId, fileId);
        FileEntity file = mappingOpt.get().getFile();

        // 매핑 삭제
        mappingRepository.deleteByAccountIdAndFileIdDirect(accountId, fileId);

        // 파일 물리 삭제
        String path = Paths.get(file.getFilePath(), file.getStoredName()).toString();
        fileUtils.deleteFile(path);

        if (file.getFileThumbName() != null) {
            String thumbPath = Paths.get(file.getFilePath(), "thumb", file.getFileThumbName()).toString();
            fileUtils.deleteFile(thumbPath);
        }

        // 파일 삭제
        long refs = mappingRepository.countByFile_FileId(fileId);
        if (refs == 0) {
            fileRepository.deletePhysicalFile(fileId);
        }
    } 

    // 가계부 상세조회
    @Transactional
    public AccountBookDTO.Detail getAccount(String userId,int accountId) throws Exception {

        // 사용자 확인
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        // 기존 엔티티 조회
        AccountBookEntity account = accountBookRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("해당 가계부 내역을 찾을 수 없습니다."));

        RecurringSettingEntity recurring = recurringRepository.findByAccount(account).orElse(null);
        InstallmentSettingEntity installment = installmentRepository.findByAccount(account).orElse(null);

        // DTO 변환
        AccountBookDTO.Detail detail = AccountBookDTO.Detail.of(account, filePath, recurring, installment);

        return detail;
    }

    // 가계부 삭제
    @Transactional
    public AccountBookDTO.Detail deleteAccount(String userId, int accountId) throws Exception{

        // 기존 엔티티 조회
        AccountBookEntity account = accountBookRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("해당 가계부 내역을 찾을 수 없습니다."));
        // 해당 가계부 사용자의 것인지 확인
        if(!account.getUser().getUserId().equals(userId)){
            throw new IllegalArgumentException("해당 가계부에 대한 권한이 없습니다.");
        }

        // 반복설정/할부설정 존재하면 삭제
        RecurringSettingEntity recurring = recurringRepository.findByAccount(account).orElse(null);
        InstallmentSettingEntity installment = installmentRepository.findByAccount(account).orElse(null);

        // dto 변경
        AccountBookDTO.Detail detail = AccountBookDTO.Detail.of(account, filePath, recurring, installment);

        // 물리 파일 정보
        List<FileEntity> filesToDelete = new ArrayList<>();
        for (AccountFileMappingEntity mapping : account.getFiles()) {
            filesToDelete.add(mapping.getFile());
        }
        
        // 가계부 삭제
        accountBookRepository.delete(account);
        accountBookRepository.flush();
        
        // 물리 파일 삭제
        for (FileEntity file : filesToDelete) {
            try {

                fileRepository.delete(file);

                String path = Paths.get(file.getFilePath(), file.getStoredName()).toString();
                String thumbPath = Paths.get(file.getFilePath(), "thumb", file.getFileThumbName()).toString();
                
                fileUtils.deleteFile(path);
                fileUtils.deleteFile(thumbPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

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

    // 차트데이터
    @Transactional
    public List<AccountBookDTO.chartResponse> getChart(String userId) throws Exception {

        // 사용자 확인
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 해당 유저의 모든 가계부 항목 불러오기
        List<AccountBookEntity> accountList = accountBookRepository.findByUser(user);

        // DTO 변환
        List<AccountBookDTO.chartResponse> result = accountList.stream()
                                                .map(AccountBookDTO.chartResponse::of)
                                                .collect(Collectors.toList());

        return result;
    }

    // 다음 반복일 계산
    public LocalDate calculateNextRecurringDate(LocalDate startDate, AccountBookDTO.RecurringDTO recurring) {
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
