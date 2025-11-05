package back.code.accountBook.service;

import back.code.accountBook.dto.AccountBookDTO;
import back.code.accountBook.dto.AccountProjection;
import back.code.accountBook.entity.AccountBookEntity;
import back.code.accountBook.repository.AccountBookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountListService {

    private final AccountBookRepository accountBookRepository;

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

}

