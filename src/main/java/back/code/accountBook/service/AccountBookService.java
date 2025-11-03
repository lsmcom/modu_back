package back.code.accountBook.service;

import back.code.accountBook.dto.AccountBookDTO;
import back.code.accountBook.entity.AccountBookEntity;
import back.code.accountBook.repository.AccountBookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountBookService {

    private final AccountBookRepository accountBookRepository;

    // 가계부 리스트(일별) 조회
    public List<AccountBookDTO.Response> getDailyAccountList(String userId, LocalDate date) throws  Exception {

        if (userId == null || date == null) {
            throw new IllegalArgumentException("userId와 date는 필수값입니다.");
        }

        List<AccountBookEntity> entities =
                accountBookRepository.findByUserId_UserIdAndDate(userId, date);

        List<AccountBookDTO.Response> accountList =entities.stream().map(AccountBookDTO.Response::of).toList();

        return accountList;
    }

    // 가계부 리스트(주별) 조회
    public List<AccountBookDTO.Response> getWeeklyAccountList(String userId, LocalDate date) throws  Exception {

        if (userId == null || date == null) {
            throw new IllegalArgumentException("userId와 date는 필수값입니다.");
        }

        List<AccountBookEntity> entities =
                accountBookRepository.findByUserId_UserIdAndDate(userId, date);

        List<AccountBookDTO.Response> accountList =entities.stream().map(AccountBookDTO.Response::of).toList();

        return accountList;
    }

    // 가계부 리스트(월별) 조회
    public List<AccountBookDTO.Response> getMonthlyAccountList(String userId, LocalDate date) throws  Exception {

        if (userId == null || date == null) {
            throw new IllegalArgumentException("userId와 date는 필수값입니다.");
        }

        List<AccountBookEntity> entities =
                accountBookRepository.findByUserId_UserIdAndDate(userId, date);

        List<AccountBookDTO.Response> accountList =entities.stream().map(AccountBookDTO.Response::of).toList();

        return accountList;
    }

}
