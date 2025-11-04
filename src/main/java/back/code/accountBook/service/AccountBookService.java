package back.code.accountBook.service;

import back.code.accountBook.dto.AccountBookDTO;
import back.code.accountBook.entity.AccountBookEntity;
import back.code.accountBook.repository.AccountBookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
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

        List<AccountBookDTO.Response> accountList =entities.stream().map(AccountBookDTO.Response::of).toList();

        return accountList;
    }

    // 가계부 리스트(주별) 조회
    @Transactional
    public List<AccountBookDTO.Response> getWeeklyAccountList(String userId, LocalDate date) throws  Exception {

        if (userId == null || date == null) {
            throw new IllegalArgumentException("userId와 date는 필수값입니다.");
        }

        LocalDate startOfWeek = date.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = date.with(DayOfWeek.SUNDAY);

        List<AccountBookEntity> entities =
                accountBookRepository.findWeeklyByUserIdAndDateBetween(userId, startOfWeek,endOfWeek);

        List<AccountBookDTO.Response> accountList =entities.stream().map(AccountBookDTO.Response::of).toList();

        return accountList;
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
    public List<AccountBookDTO.CalendarAccountResponse> getCalendarAccountList(String userId, LocalDate date) throws  Exception {

        if (userId == null || date == null) {
            throw new IllegalArgumentException("userId와 date는 필수값입니다.");
        }

        Object[] projection = accountBookRepository.findCalendarByUserIdAndDate(userId, date);
        if (projection == null) {
            return Collections.emptyList();
        }

        return Collections.singletonList(
                AccountBookDTO.CalendarAccountResponse.of(projection)
        );
    }

}
