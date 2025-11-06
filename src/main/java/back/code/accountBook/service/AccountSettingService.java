package back.code.accountBook.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import back.code.accountBook.dto.AccountSavingGoalDTO;
import back.code.accountBook.entity.AccountSavingsGoalEntity;
import back.code.accountBook.repository.SavingGoalRepository;
import back.code.user.entity.UserEntity;
import back.code.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountSettingService {

    private final UserRepository userRepository;
    private final SavingGoalRepository savingGoalRepository;

    // 작성창용 저축목표
    @Transactional
    public List<AccountSavingGoalDTO.writeGoals> getWriteGoals(String userId) throws Exception{

        // 사용자 확인
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        // 사용자의 저축 목표 조회
        List<AccountSavingsGoalEntity> goals = savingGoalRepository.findAllByUser(user);
        // DTO 변환
        List<AccountSavingGoalDTO.writeGoals> result = new ArrayList<>();
        for (AccountSavingsGoalEntity goal : goals) {
            result.add(AccountSavingGoalDTO.writeGoals.of(goal));
        }

        return result;            
    }

}
