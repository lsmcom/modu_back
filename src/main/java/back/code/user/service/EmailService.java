package back.code.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    // 메모리 캐시
    private final Map<String, String> authCodeMap = new ConcurrentHashMap<>();

    private static final int CODE_LENGTH = 6;
    private static final String SUBJECT = "[MODU] 이메일 인증 코드";

    /** 인증 코드 생성 */
    private String generateAuthCode() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(random.nextInt(10)); // 0~9
        }
        return sb.toString();
    }

    /** 이메일 전송 및 코드 저장 */
    public void sendAuthCode(String toEmail) {
        String authCode = generateAuthCode();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(SUBJECT);
        message.setText("인증번호: " + authCode + "\n\n5분 이내에 입력해주세요.");

        mailSender.send(message);

        // 인증 코드 메모리 저장 (5분 유효)
        authCodeMap.put(toEmail, authCode);
        log.info("[이메일 인증코드 전송] email={}, code={}", toEmail, authCode);
    }

    /** 인증 코드 검증 */
    public boolean verifyAuthCode(String email, String code) {
        String savedCode = authCodeMap.get(email);
        if (savedCode == null) return false;
        boolean match = savedCode.equals(code);
        if (match) authCodeMap.remove(email); // 사용 후 제거
        return match;
    }
}
