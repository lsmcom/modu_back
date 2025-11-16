package back.code.user.service;

import back.code.accountBook.repository.AccountBookRepository;
import back.code.accountBook.repository.BudgetRepository;
import back.code.accountBook.repository.CategoryRepository;
import back.code.accountBook.repository.SavingGoalRepository;
import back.code.calendar.repository.CalendarFolderRepository;
import back.code.calendar.repository.CalendarSettingRepository;
import back.code.calendar.service.CalendarFolderService;
import back.code.file.repository.FileRepository;
import back.code.milestone.repository.UserMilestoneRepository;
import back.code.notice.repository.NotificationRepository;
import back.code.recentsearch.repository.RecentSearchRepository;
import back.code.todo.repository.TodoFolderRepository;
import back.code.todo.service.TodoFolderService;
import back.code.memo.repository.MemoFolderRepository;
import back.code.memo.service.MemoFolderService;
import back.code.user.dto.UserSettingUpdateDTO;
import back.code.user.entity.UserEntity;
import back.code.user.entity.UserSettingEntity;
import back.code.user.repository.UserRepository;
import back.code.user.repository.UserSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSettingService {

    private final CalendarFolderService calendarFolderService;

    private final UserRepository userRepository;
    private final UserSettingRepository userSettingRepository;
    private final AccountBookRepository accountBookRepository;
    private final BudgetRepository budgetRepository;
    private final SavingGoalRepository savingGoalRepository;
    private final CategoryRepository categoryRepository;
    private final MemoFolderRepository memoFolderRepository;
    private final MemoFolderService memoFolderService;
    private final CalendarFolderRepository calendarFolderRepository;
    private final CalendarSettingRepository calendarSettingRepository;
    private final TodoFolderRepository todoFolderRepository;
    private final TodoFolderService todoFolderService;
    private final RecentSearchRepository recentSearchRepository;
    private final UserMilestoneRepository userMilestoneRepository;
    private final NotificationRepository notificationRepository;
    private final FileRepository fileRepository;


    /** 회원가입시 기본 사용자 설정 생성 */
    @Transactional
    public void createDefaultSetting(UserEntity user) {
        UserSettingEntity setting = UserSettingEntity.builder()
                .settingId(UUID.randomUUID().toString())
                .user(user)
                .theDayOfWeek("M")           // 기본 월요일
                .themeMode("light")          // 기본 라이트 모드
                .alarmAllowed("Y")           // 알림 허용
                .personalInfoAgreed("Y")     // 개인정보 동의
                .locationInfoAgreed("Y")     // 위치정보 동의
                .marketingInfoAgreed("N")    // 마케팅 동의
                .build();

        userSettingRepository.save(setting);
        log.info("[기본 설정 생성 완료] userId={}", user.getUserId());
    }

    /** 사용자 설정 조회 */
    @Transactional(readOnly = true)
    public UserSettingEntity getUserSetting(String userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));
        return userSettingRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("사용자 설정이 존재하지 않습니다."));
    }

    /** 사용자 설정 수정 */
    @Transactional
    public void updateUserSetting(String userId, UserSettingUpdateDTO dto) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

        UserSettingEntity setting = userSettingRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("사용자 설정이 존재하지 않습니다."));

        if (dto.getTheDayOfWeek() != null)
            setting.setTheDayOfWeek(dto.getTheDayOfWeek());
        if (dto.getThemeMode() != null)
            setting.setThemeMode(dto.getThemeMode());
        if (dto.getAlarmAllowed() != null)
            setting.setAlarmAllowed(dto.getAlarmAllowed());
        if (dto.getMarketingInfoAgreed() != null)
            setting.updateMarketingAgree(dto.getMarketingInfoAgreed());

        userSettingRepository.save(setting);

        log.info("[사용자 설정 변경 완료] userId={}, dto={}", userId, dto);
    }

    /** 데이터 초기화 */
    @Transactional
    public void resetData(String userId) throws Exception {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

        // 사용자 설정 초기화
        userSettingRepository.deleteByUser(user);
        createDefaultSetting(user);
            
        // 가계부 데이터 삭제
        budgetRepository.deleteByUser(user);
        accountBookRepository.deleteByUser(user);
        savingGoalRepository.deleteByUser(user);
        categoryRepository.deleteByUserAndIsDefaultNull(user);  // 사용자 카테고리만 초기화

        // 메모 데이터 삭제 및 초기화
        memoFolderRepository.deleteByUser(user);
        memoFolderService.createDefaultFolders(user);

        // 캘린더 데이터 삭제 및 초기화
        calendarFolderRepository.deleteByUser(user);
        calendarSettingRepository.deleteByUser(user);  // 알아서 초기화 됨
        calendarFolderService.createDefaultFolders(user);  // 캘린더 공유폴더 자동 생성

        // 투두 데이터 삭제 및 초기화
        todoFolderRepository.deleteByUserId(user.getUserId());
        todoFolderService.createDefaultTodoFolders(user);

        // 파일 삭제
        fileRepository.deleteByUser(user);

        // 알림 삭제
        notificationRepository.deleteByUserId(user.getUserId());

        // 업적 삭제
        userMilestoneRepository.deleteByUserId(user.getUserId());

        // 최근검색어 삭제
        recentSearchRepository.deleteByUserId(user.getUserId());
        
    }
}
