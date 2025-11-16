package back.code.user.service;

import back.code.accountBook.service.AccountCategoryService;
import back.code.calendar.service.CalendarFolderService;
import back.code.file.dto.FileDTO;
import back.code.file.entity.FileEntity;
import back.code.file.repository.FileRepository;
import back.code.file.service.FileService;
import back.code.memo.service.MemoFolderService;
import back.code.todo.service.TodoFolderService;
import back.code.user.dto.JoinRequestDTO;
import back.code.user.dto.UserInfoDTO;
import back.code.user.dto.UserUpdateRequest;
import back.code.user.entity.UserEntity;
import back.code.user.entity.UserRoleEntity;
import back.code.user.repository.UserRepository;
import back.code.user.repository.UserRoleRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final UserRoleRepository userRoleRepository;
    private final EmailService emailService;
    private final UserSettingService userSettingService;
    private final FileRepository fileRepository;
    private final FileService fileService;
    private final CalendarFolderService calendarFolderService;
    private final MemoFolderService memoFolderService;
    private final AccountCategoryService categoryService;
    private final TodoFolderService todoFolderService;

    /** 아이디 중복 확인 */
    @Transactional(readOnly = true)
    public String checkDuplicateUserId(String userId) {
        if (userRepository.existsById(userId)) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다");
        }
        return "사용 가능한 아이디입니다";
    }

    /** 이메일 중복 확인 + 인증코드 전송 */
    @Transactional
    public String checkDuplicateEmailAndSendCode(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다");
        }

        // 중복이 아닐 경우 이메일 인증 코드 발송
        emailService.sendAuthCode(email);
        return "이메일로 인증번호를 전송했습니다.";
    }

    /** 이메일 인증번호 검증 */
    public String verifyEmailCode(String email, String code) {
        boolean valid = emailService.verifyAuthCode(email, code);
        if (!valid) throw new IllegalArgumentException("인증번호가 일치하지 않습니다.");
        return "이메일 인증이 완료되었습니다.";
    }

    /** 닉네임 중복 확인 */
    @Transactional(readOnly = true)
    public String checkDuplicateNick(String nick) {
        if (userRepository.findByUserNick(nick).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 닉네임입니다");
        }
        return "사용 가능한 닉네임입니다";
    }

    /** 전화번호 정규화: 숫자만 남기기 */
    private String normalizePhone(String raw) {
        return raw == null ? null : raw.replaceAll("\\D", "");
    }

    /** 전화번호 중복 확인 */
    @Transactional(readOnly = true)
    public String checkDuplicatePhone(String phone) {
        String normalized = normalizePhone(phone);
        int result = userRepository.existsByPhoneNormalized(normalized);
        if (result == 1) {
            throw new IllegalArgumentException("이미 등록된 전화번호입니다");
        }
        return "사용 가능한 전화번호입니다";
    }

    // 회원가입
    @Transactional
    public void join(JoinRequestDTO dto) {

        // 중복 체크
        if (userRepository.existsById(dto.getUserId())) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다");
        }
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다");
        }
        if (userRepository.findByUserNick(dto.getUserNick()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 닉네임입니다");
        }
        String normalized = normalizePhone(dto.getPhone());
        int result = userRepository.existsByPhoneNormalized(normalized);
        if (result == 1) {
            throw new IllegalArgumentException("이미 등록된 전화번호입니다");
        }

        // 생년월일 변환
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd");
        LocalDate birth = LocalDate.parse(dto.getBirth(), formatter);

        // 권한 엔티티 조회
        UserRoleEntity userRole = userRoleRepository.findById("USER")
                .orElseThrow(() -> new RuntimeException("기본 권한(USER)을 찾을 수 없습니다."));

        // 엔티티 생성
        UserEntity user = new UserEntity();
        user.setUserId(dto.getUserId());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setUserName(dto.getUserName());
        user.setUserNick(dto.getUserNick());
        user.setEmail(dto.getEmail());
        user.setBirth(birth);
        user.setAgency(dto.getAgency());
        user.setPhone(dto.getPhone());
        user.setAddr(dto.getAddr());
        user.setAddrDetail(dto.getAddrDetail());
        user.setUserRole(userRole);
        user.setStatus("active");

        // 저장
        userRepository.save(user);
        // flush 실행하여 save 기능 밀림 해결
        userRepository.flush();
        // 기본 사용자 설정 생성
        userSettingService.createDefaultSetting(user);

        // 캘린더 폴더 자동 생성
        calendarFolderService.createDefaultFolders(user);

        // Todo 폴더 자동 생성
        todoFolderService.createDefaultTodoFoldersInNewTx(user);

        // 메모 폴더 자동 생성
        memoFolderService.createDefaultFolders(user);

        // 가계부 기본 카테고리 자동 생성
        categoryService.createDefaultCategory(user);

        log.info("[회원가입 성공] userId={}, email={}", dto.getUserId(), dto.getEmail());
    }

    /**
     * 아이디 찾기 - 이메일로 인증번호 발송
     */
    @Transactional(readOnly = true)
    public void sendFindIdEmail(String email) throws MessagingException {
        // 가입된 이메일인지 확인
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("가입되지 않은 이메일입니다."));

        // 탈퇴 회원 차단
        if ("withdrawn".equalsIgnoreCase(user.getStatus())) {
            throw new RuntimeException("탈퇴한 회원은 아이디 찾기를 이용할 수 없습니다.");
        }

        // 인증번호 전송
        emailService.sendAuthCode(email);
        log.info("[아이디 찾기] 인증번호 발송 완료 - email={}", email);
    }

    /**
     * 인증번호 검증 후 아이디 반환
     */
    @Transactional(readOnly = true)
    public String verifyFindIdCode(String email, String code) {
        boolean verified = emailService.verifyAuthCode(email, code);
        if (!verified) {
            throw new RuntimeException("인증번호가 올바르지 않습니다.");
        }

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("가입되지 않은 이메일입니다."));

        log.info("[아이디 찾기 성공] email={}, userId={}", email, user.getUserId());
        return user.getUserId();
    }

    /**
     * 비밀번호 찾기 - 아이디 + 이메일 확인 후 인증번호 발송
     */
    @Transactional(readOnly = true)
    public void sendFindPwEmail(String userId, String email) throws MessagingException {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 아이디입니다."));

        if (!user.getEmail().equals(email)) {
            throw new RuntimeException("입력한 이메일이 해당 아이디와 일치하지 않습니다.");
        }

        // 탈퇴 회원 차단
        if ("withdrawn".equalsIgnoreCase(user.getStatus())) {
            throw new RuntimeException("탈퇴한 회원은 비밀번호 찾기를 이용할 수 없습니다.");
        }

        emailService.sendAuthCode(email);
        log.info("[비밀번호 찾기] 인증번호 발송 완료 - userId={}, email={}", userId, email);
    }

    /**
     * 인증번호 검증 (비밀번호 재설정 단계 진입)
     */
    @Transactional(readOnly = true)
    public void verifyFindPwCode(String userId, String email, String code) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 아이디입니다."));

        if (!user.getEmail().equals(email)) {
            throw new RuntimeException("입력한 이메일이 해당 아이디와 일치하지 않습니다.");
        }

        boolean verified = emailService.verifyAuthCode(email, code);
        if (!verified) {
            throw new RuntimeException("인증번호가 올바르지 않습니다.");
        }

        log.info("[비밀번호 찾기 인증 성공] userId={}, email={}", userId, email);
    }

    /**
     * 비밀번호 재설정
     */
    @Transactional
    public void resetPassword(String userId, String newPassword) {
        // 존재하는 사용자 확인
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 아이디입니다."));

        // 비밀번호 유효성 검사 (프론트에서 1차 검증했지만 안전을 위해 백엔드에서도)
        if (newPassword == null || newPassword.length() < 8) {
            throw new RuntimeException("비밀번호는 최소 8자 이상이어야 합니다.");
        }

        // 기존 비밀번호와 동일한 경우 방지
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new RuntimeException("기존 비밀번호와 동일한 비밀번호는 사용할 수 없습니다.");
        }

        // 새 비밀번호 암호화 후 저장
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("[비밀번호 재설정 완료] userId={}", userId);
    }

    /**
     * 사용자 프로필 이미지 조회
     */
    @Transactional(readOnly = true)
    public String getProfileImage(String userId) {
        // 유효성 검사
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("유효하지 않은 사용자 ID입니다.");
        }

        // 파일 조회
        List<FileEntity> files = fileRepository.findByUser_UserIdAndFileType(userId, "PROFILE");
        if (files.isEmpty()) {
            log.info("[PROFILE] userId={} 프로필 이미지 없음", userId);
            return null;
        }

        // 최신 파일(가장 최근 createAt 기준) 선택
        FileEntity latest = files.stream()
                .max(Comparator.comparing(FileEntity::getCreateAt))
                .orElseThrow(() -> new RuntimeException("프로필 이미지 조회 중 오류가 발생했습니다."));

        // 경로 문자열 반환
        String fullPath = Paths.get(latest.getFilePath(), latest.getStoredName())
                .toString()
                .replace("\\", "/");

        log.info("[PROFILE] userId={} 최신 프로필 이미지 반환: {}", userId, fullPath);
        return fullPath;
    }

    /**
     * 프로필 이미지 변경
     */
    public FileDTO updateProfileImage(String userId, MultipartFile newFile) throws IOException {
        if (newFile == null || newFile.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 존재하지 않습니다.");
        }

        // 유저 존재 여부 확인
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 기존 프로필 파일 조회
        List<FileEntity> existingFiles = fileRepository.findByUser_UserIdAndFileType(userId, "PROFILE");

        // 기존 파일 삭제
        for (FileEntity oldFile : existingFiles) {
            fileService.deleteFileEntity(oldFile);
        }

        // 새 프로필 업로드
        FileDTO uploaded = fileService.uploadFile(newFile, userId, "PROFILE");
        log.info("[PROFILE] 사용자 {} 새 프로필 업로드 완료: {}", userId, uploaded.getStoredName());

        return null;
    }

    /**
     * 닉네임 조회
     */
    public String getUserNickname(String userId) {
        return userRepository.findById(userId)
                .map(UserEntity::getUserNick)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    }

    /**
     * 닉네임 변경
     */
    @Transactional
    public void updateNickname(String userId, String newNick) {
        if (newNick == null || newNick.trim().isEmpty()) {
            throw new IllegalArgumentException("닉네임을 입력해주세요.");
        }

        // 유저 존재 확인
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 중복 닉네임 확인
        boolean exists = userRepository.existsByUserNick(newNick);
        if (exists) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        // 닉네임 변경
        user.setUserNick(newNick);
        userRepository.save(user);

        log.info("[USER] 닉네임 변경 완료 - userId={}, newNick={}", userId, newNick);
    }

    /** 회원정보 조회 */
    @Transactional
    public UserInfoDTO getUserInfo(String userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        return UserInfoDTO.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .email(user.getEmail())
                .birth(user.getBirth())
                .agency(user.getAgency())
                .phone(user.getPhone())
                .addr(user.getAddr())
                .addrDetail(user.getAddrDetail())
                .build();
    }

    /** 회원정보 수정 (비밀번호/닉네임 제외) */
    @Transactional
    public void updateUserInfo(String userId, UserUpdateRequest req) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 이메일 변경 시, 인증이 완료된 상태여야 함 (프론트에서 verifyEmailCode() 완료 후 요청)
        if (!user.getEmail().equals(req.getEmail())) {
            log.info("[회원정보 수정] 이메일 변경 감지 → 인증완료 이메일로 업데이트");
            user.setEmail(req.getEmail());
        }

        user.setUserName(req.getUserName());
        user.setBirth(req.getBirth());
        user.setAgency(req.getAgency());
        user.setPhone(req.getPhone());
        user.setAddr(req.getAddr());
        user.setAddrDetail(req.getAddrDetail());

        userRepository.save(user);
        log.info("[회원정보 수정 완료] userId={}, email={}", userId, req.getEmail());
    }

    // 비밀번호 변경
    @Transactional
    public void editPassword(String userId, String currentPassword, String newPassword) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 아이디입니다."));

        if (currentPassword == null || currentPassword.isBlank()) {
            throw new RuntimeException("현재 비밀번호를 입력해주세요.");
        }
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("현재 비밀번호가 일치하지 않습니다.");
        }

        if (newPassword == null || newPassword.length() < 8) {
            throw new RuntimeException("비밀번호는 최소 8자 이상이어야 합니다.");
        }
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new RuntimeException("기존 비밀번호와 동일한 비밀번호는 사용할 수 없습니다.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("[비밀번호 재설정 완료] userId={}", userId);
    }

    /** 회원탈퇴 */
    @Transactional
    public void withdrawUser(String userId, String password, String reason) {
        // 사용자 조회
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

        // 비밀번호 검증
        if (password == null || password.isBlank()) {
            throw new RuntimeException("비밀번호를 입력해주세요.");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        // 이미 탈퇴된 회원 방지
        if ("withdrawn".equalsIgnoreCase(user.getStatus())) {
            throw new RuntimeException("이미 탈퇴한 회원입니다.");
        }

        // 탈퇴 처리
        user.setWithdrawAt(Instant.now());
        user.setWithdrawReason(reason);
        user.setStatus("withdrawn"); // 상태를 '탈퇴' 상태로 변경

        userRepository.save(user);
        log.info("[회원탈퇴 완료] userId={}, reason={}", userId, reason);
    }
}

