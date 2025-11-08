package back.code.accountBook.service;

import back.code.accountBook.dto.AccountBookDTO;
import back.code.accountBook.dto.AccountProjection;
import back.code.accountBook.entity.AccountBookEntity;
import back.code.accountBook.entity.InstallmentSettingEntity;
import back.code.accountBook.repository.AccountBookRepository;
import back.code.user.entity.UserEntity;
import back.code.user.entity.UserSettingEntity;
import back.code.user.repository.UserRepository;
import back.code.user.repository.UserSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountListService {

    private final AccountBookRepository accountBookRepository;
    private final UserSettingRepository userSettingRepository;
    private final UserRepository userRepository;

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
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        UserSettingEntity userSetting = userSettingRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("사용자 설정을 찾을 수 없습니다."));

        // 주 시작일 매핑
        DayOfWeek startDayOfWeek =
                userSetting.getTheDayOfWeek().equalsIgnoreCase("S") ? DayOfWeek.SUNDAY : DayOfWeek.MONDAY;

        int weekStartNumber = startDayOfWeek == DayOfWeek.SUNDAY ? 1 : 2;

        // 해당 달의 시작일과 마지막일
        LocalDate startOfMonth = date.withDayOfMonth(1);
        LocalDate endOfMonth = date.withDayOfMonth(date.lengthOfMonth());

        // 해당 월 1일이 속한 주의 시작일
        LocalDate weekStartDate = startOfMonth.with(TemporalAdjusters.previousOrSame(startDayOfWeek));
        // 해당 월 마지막 날이 속한 주의 종료일
        LocalDate weekEndDate = endOfMonth.with(TemporalAdjusters.nextOrSame(startDayOfWeek)).plusDays(6);

        List<AccountProjection> projections  =
                accountBookRepository.findWeeklyByUserIdAndDateBetween(userId, weekStartDate, weekEndDate, weekStartNumber);

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

    // 할부내역 리스트
    @Transactional
    public List<AccountBookDTO.InstallmentListResponse> getInstallmentList(String userId) throws Exception{

        if (userId == null) {
           throw new IllegalArgumentException("userId는 필수값입니다.");
        }

        List<InstallmentSettingEntity> installments = 
                            accountBookRepository.findAllByUserIdWithAccount(userId);

        List<AccountBookDTO.InstallmentListResponse> result = installments.stream()
                        .map(installment -> {
                            AccountBookEntity account = installment.getAccount();

                            // 현재 회차 계산
                            int currentMonth = calculateCurrentMonth(installment);
                            // 완료 여부 계산
                            boolean isCompleted = currentMonth >= installment.getTotalMonths();
                            // 잔여 금액 계산
                            int remainingAmount = calculateRemainingAmount(installment, currentMonth);
                            
                            return AccountBookDTO.InstallmentListResponse.of(
                                account, 
                                installment,
                                currentMonth, 
                                isCompleted, 
                                remainingAmount
                            );
                        })
                        .collect(Collectors.toList());

        return result;
    }

    // 현재 회차 계산
    private int calculateCurrentMonth(InstallmentSettingEntity installment) {
        LocalDate start = installment.getStartDate();
        LocalDate now = LocalDate.now();

        // 시작일 이후 지난 개월 수 + 1 (시작월을 1회차로)
        int currentMonth = now.getMonthValue() - start.getMonthValue() + 
                        (now.getYear() - start.getYear()) * 12 + 1;

        // 음수면 0, 총 개월수보다 크면 총 개월수
        if (currentMonth < 0) currentMonth = 0;
        if (currentMonth > installment.getTotalMonths()) currentMonth = installment.getTotalMonths();

        return currentMonth;
    }

    // 잔여 금액 계산
    private int calculateRemainingAmount(InstallmentSettingEntity installment, int currentMonth) {
        int paidAmount = currentMonth * installment.getMonthlyAmount();
        return installment.getTotalAmount() - paidAmount;
    }

}

