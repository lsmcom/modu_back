package back.code.user.service;

import back.code.user.dto.JoinRequestDTO;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final UserRoleRepository userRoleRepository;
    private final EmailService emailService;

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

        log.info("[회원가입 성공] userId={}, email={}", dto.getUserId(), dto.getEmail());
    }

    /**
     * 아이디 찾기 - 이메일로 인증번호 발송
     */
    @Transactional(readOnly = true)
    public void sendFindIdEmail(String email) throws MessagingException {
        // 가입된 이메일인지 확인
        if (userRepository.findByEmail(email).isEmpty()) {
            throw new RuntimeException("가입되지 않은 이메일입니다.");
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
}
