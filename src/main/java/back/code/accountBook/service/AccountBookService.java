package back.code.accountBook.service;

import back.code.accountBook.dto.AccountBookDTO;
import back.code.accountBook.dto.AccountCategoryDTO;
import back.code.accountBook.dto.AccountProjection;
import back.code.accountBook.entity.AccountBookEntity;
import back.code.accountBook.entity.AccountCategoryEntity;
import back.code.accountBook.entity.AccountSavingsGoalEntity;
import back.code.accountBook.repository.AccountBookRepository;
import back.code.accountBook.repository.AccountFileMappingRepository;
import back.code.accountBook.repository.CategoryRepository;
import back.code.accountBook.repository.SavingGoalRepository;
import back.code.user.entity.UserEntity;
import back.code.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountBookService {

    private final UserRepository userRepository;
    private final AccountBookRepository accountBookRepository;
    private final AccountFileMappingRepository accountFileMappingRepository;
    private final CategoryRepository categoryRepository;
    private final SavingGoalRepository savingGoalRepository;

    // 가계부 리스트(일별) 조회
    @Transactional
    public List<AccountBookDTO.Response> getDailyAccountList(String userId, LocalDate date) throws  Exception {

        if (userId == null || date == null) {
            throw new IllegalArgumentException("userId와 date는 필수값입니다.");
        }

        // 이번 달의 시작일과 마지막일 계산
        LocalDate startOfMonth = date.withDayOfMonth(1);
        LocalDate endOfMonth = date.withDayOfMonth(date.lengthOfMonth());

        List<AccountBookEntity> entities =
                accountBookRepository.findDailyByUserIdAndDateBetween(userId, startOfMonth,endOfMonth);

        List<AccountBookDTO.Response> result =entities.stream().map(AccountBookDTO.Response::of).toList();

        return result;
    }

    // 가계부 리스트(주별) 조회
    @Transactional
    public List<AccountBookDTO.WeekResponse> getWeeklyAccountList(String userId, LocalDate date) throws  Exception {

        if (userId == null || date == null) {
            throw new IllegalArgumentException("userId와 date는 필수값입니다.");
        }

        // 해당 달의 시작일과 마지막일
        LocalDate startOfMonth = date.withDayOfMonth(1);
        LocalDate endOfMonth = date.withDayOfMonth(date.lengthOfMonth());

        List<AccountProjection> projections  = accountBookRepository.findWeeklyByUserIdAndDateBetween(userId, startOfMonth, endOfMonth);

        List<AccountBookDTO.WeekResponse> result = new ArrayList<>();
        for (AccountProjection projection : projections ) {
            result.add(AccountBookDTO.WeekResponse.of(projection));
        }

        return result;
    }

    // 가계부 리스트(월별) 조회
   @Transactional
   public List<AccountBookDTO.MonthResponse> getMonthlyAccountList(String userId) throws  Exception {

       if (userId == null) {
           throw new IllegalArgumentException("userId는 필수값입니다.");
       }

       List<AccountProjection> projections  = accountBookRepository.findMonthlyByUserId(userId);

       List<AccountBookDTO.MonthResponse> result = new ArrayList<>();
        for (AccountProjection projection : projections ) {
            result.add(AccountBookDTO.MonthResponse.of(projection));
        }

       return result;
   }

    // 가계부 리스트(달력) 조회
    @Transactional
    public AccountBookDTO.CalendarAccountResponse getCalendarAccountList(String userId, LocalDate date) throws  Exception {

        if (userId == null || date == null) {
            throw new IllegalArgumentException("userId와 date는 필수값입니다.");
        }

        // 해당 월의 첫날과 마지막날 계산
        LocalDate startDate = date.withDayOfMonth(1);
        LocalDate endDate = date.withDayOfMonth(date.lengthOfMonth());

        // 일별 데이터 조회 (Projection 사용)
        List<AccountProjection> projections  =
                accountBookRepository.findDailyByUserIdAndDateRange(userId, startDate, endDate);

        // 총합 계산 + 일별 리스트 생성
        int totalIncome = 0;
        int totalExpense = 0;
        List<AccountBookDTO.CalendarAccountResponse.DailyAccount> dailyList = new ArrayList<>();

        for (AccountProjection projection : projections ) {
            AccountBookDTO.CalendarAccountResponse.DailyAccount daily =
                    AccountBookDTO.CalendarAccountResponse.DailyAccount.of(projection);

            totalIncome += daily.getIncome();
            totalExpense += daily.getExpense();

            dailyList.add(daily);
        }

        AccountBookDTO.CalendarAccountResponse result = AccountBookDTO.CalendarAccountResponse.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .dailyList(dailyList)
                .build();

        return result;
    }

    // 가계부 작성
    @Transactional
    public AccountBookEntity  writeAccount(AccountBookDTO.Request request) throws Exception{

        // 사용자 확인
        UserEntity user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 카테고리 확인
        AccountCategoryEntity category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다."));

        // 저축 목표 확인 (선택사항일 수도 있음)
        AccountSavingsGoalEntity savingGoal = null;
        if (request.getSavingGoalId() != null) {
            savingGoal = savingGoalRepository.findById(request.getSavingGoalId())
                    .orElseThrow(() -> new RuntimeException("저축 목표를 찾을 수 없습니다."));
        }

        // DTO → 엔티티
        AccountBookEntity account = request.to(new ArrayList<>(),user, category, savingGoal);

        // 저장
        accountBookRepository.save(account);

        return account;

    }

    // 카테고리 조회
    @Transactional
    public List<AccountCategoryEntity> categoryList(String userId) throws Exception{

        // 사용자 확인
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        // 기본 카테고리
        List<AccountCategoryEntity> defaultCategories = categoryRepository.findAllByIsDefaultTrue();
        // 사용자 정의 카테고리
        List<AccountCategoryEntity> userCategories = categoryRepository.findAllByUser(user);

        List<AccountCategoryEntity> allCategories = new ArrayList<>();
        allCategories.addAll(defaultCategories);
        allCategories.addAll(userCategories);

        return allCategories;
    }

    // 카테고리 추가
    @Transactional
    public AccountCategoryEntity categoryAdd(AccountCategoryDTO.Request request) throws Exception{

        // 사용자 확인
        UserEntity user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        // DTO → 엔티티
        AccountCategoryEntity category = request.to(user);
        // 저장
        categoryRepository.save(category);

        return category;
    }

    // 카테고리 수정
    @Transactional
    public AccountCategoryEntity categoryUpdate(AccountCategoryDTO.Request request) throws Exception{

        // 사용자 확인
        UserEntity user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        AccountCategoryEntity category;
        // 기존 카테고리 수정 및 새 카테고리 생성
        if(request.getCategoryId() != 0) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다."));
        } else {
            category = new AccountCategoryEntity();
        }
        // DTO → 엔티티
        category = request.to(user);
        // 저장
        categoryRepository.save(category);

        return category;
    }

    // 카테고리 삭제
    @Transactional
    public AccountCategoryEntity categoryDelete(String userId, int categoryId) throws Exception{

        // 사용자 확인
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        // 카테고리 확인
        AccountCategoryEntity category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다."));
        // dto로 변경
        AccountCategoryDTO.Response response = AccountCategoryDTO.Response.of(category);
        // 삭제
        categoryRepository.delete(category);

        return category;
    }

}

