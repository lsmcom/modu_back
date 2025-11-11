package back.code.accountBook.scheduler;

import back.code.accountBook.dto.AccountBookDTO;
import back.code.accountBook.entity.AccountBookEntity;
import back.code.accountBook.entity.RecurringSettingEntity;
import back.code.accountBook.repository.AccountBookRepository;
import back.code.accountBook.repository.RecurringSettingRepository;
import back.code.accountBook.service.AccountBookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RecurringAccountScheduler {

    private final RecurringSettingRepository recurringRepository;
    private final AccountBookRepository accountBookRepository;
    private final AccountBookService accountBookService;


    // 매일 자정(00:00:00)에 실행
    // 오늘이 nextDate인 반복 설정들을 찾아서 가계부 내역 자동 생성
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void createRecurringAccounts() {

        LocalDate today = LocalDate.now();

        try {
            // 오늘이 nextDate이고 활성화된 반복 설정 조회
            List<RecurringSettingEntity> recurringList =
                    recurringRepository.findByNextDateAndIsActiveTrue(today);

            int successCount = 0;
            int failCount = 0;

            for (RecurringSettingEntity recurring : recurringList) {
                try {
                    // 1. 원본 가계부 조회
                    AccountBookEntity originalAccount = recurring.getAccount();

                    if (originalAccount == null) {
                        log.warn("원본 가계부를 찾을 수 없음: recurringId={}", recurring.getRecurringId());
                        failCount++;
                        continue;
                    }

                    // 2. 새로운 가계부 내역 생성 (원본 복사)
                    AccountBookEntity newAccount = createRecurringAccount(originalAccount, today);

                    // 3. 저장
                    accountBookRepository.save(newAccount);

                    // 4. 다음 반복일 계산
                    LocalDate nextDate = accountBookService.calculateNextRecurringDate(today, AccountBookDTO.RecurringDTO.of(recurring));

                    // 5. 반복 설정 업데이트
                    recurring.setNextDate(nextDate);

                    // 6. 종료일 체크
                    if (recurring.getEndDate() != null && nextDate.isAfter(recurring.getEndDate())) {
                        recurring.setIsActive(false);
                    }

                    recurringRepository.save(recurring);
                    successCount++;

                } catch (Exception e) {
                    failCount++;
                }
            }

        } catch (Exception e) {
            log.error("반복 가계부 스케줄러 실행 중 오류 발생", e);
        }
    }


    // 원본 가계부를 복사하여 새로운 반복 내역 생성
    private AccountBookEntity createRecurringAccount(AccountBookEntity original, LocalDate newDate) {
        AccountBookEntity newAccount = new AccountBookEntity();

        // 원본 데이터 복사
        newAccount.setUser(original.getUser());
        newAccount.setCategory(original.getCategory());
        newAccount.setType(original.getType());
        newAccount.setMethod(original.getMethod());
        newAccount.setAmount(original.getAmount());
        newAccount.setContent(original.getContent() + " (반복)");
        newAccount.setDate(newDate);
        newAccount.setGoal(original.getGoal());

        return newAccount;
    }

}
