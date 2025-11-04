package back.code.accountBook.service;

import back.code.accountBook.dto.AccountBookDTO;
import back.code.accountBook.entity.AccountBookEntity;
import back.code.accountBook.repository.AccountBookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountBookService {

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

        List<Object[]> results = accountBookRepository.findByUserIdAndDateBetween(userId, startOfMonth, endOfMonth);

        List<AccountBookDTO.WeekResponse> weekList = new ArrayList<>();
        for (Object[] row : results) {
            String weekStartStr = (String) row[0];
            String weekEndStr = (String) row[1];

            LocalDate weekStart = LocalDate.parse(weekStartStr);
            LocalDate weekEnd = LocalDate.parse(weekEndStr);

            int income = ((Number) row[2]).intValue();
            int expense = ((Number) row[3]).intValue();

            weekList.add(AccountBookDTO.WeekResponse.builder()
                    .weekStartDate(weekStart.toString())
                    .weekEndDate(weekEnd.toString())
                    .income(income)
                    .expense(expense)
                    .build());
        }

        return weekList;
    }

    // 가계부 리스트(월별) 조회
//    @Transactional
//    public List<AccountBookDTO.Response> getMonthlyAccountList(String userId) throws  Exception {
//
//        if (userId == null || date == null) {
//            throw new IllegalArgumentException("userId와 date는 필수값입니다.");
//        }
//
//        // 이번 달의 시작일과 마지막일 계산
//        LocalDate startOfMonth = date.withDayOfMonth(1);
//        LocalDate endOfMonth = date.withDayOfMonth(date.lengthOfMonth());
//
//        List<AccountBookEntity> entities =
//                accountBookRepository.findDailyByUserIdAndDateBetween(userId, startOfMonth,endOfMonth);
//
//        List<AccountBookDTO.Response> accountList =entities.stream().map(AccountBookDTO.Response::of).toList();
//
//        return accountList;
//    }

    // 가계부 리스트(달력) 조회
    @Transactional
    public AccountBookDTO.CalendarAccountResponse getCalendarAccountList(String userId, LocalDate date) throws  Exception {

        if (userId == null || date == null) {
            throw new IllegalArgumentException("userId와 date는 필수값입니다.");
        }

        List<Object[]> results  = accountBookRepository.findCalendarByUserIdAndDate(userId, date);
        Object[] projection = results.isEmpty() ? new Object[]{0, 0} : results.get(0);
        return AccountBookDTO.CalendarAccountResponse.of(projection);
    }

}
