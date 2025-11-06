package back.code.accountBook.service;

import java.util.ArrayList;

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

@Service
@RequiredArgsConstructor
public class AccountBookService {

    private final UserRepository userRepository;
    private final AccountBookRepository accountBookRepository;
    private final CategoryRepository categoryRepository;
    private final SavingGoalRepository savingGoalRepository;

    // 가계부 작성
    @Transactional
    public AccountBookEntity writeAccount(AccountBookDTO.Request request) throws Exception{

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
        AccountBookEntity account = request.to(new ArrayList<>(),user, category, savingGoal);
        // 저장
        accountBookRepository.save(account);

        return account;

    }

}
