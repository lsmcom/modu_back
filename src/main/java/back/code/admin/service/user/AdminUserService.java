package back.code.admin.service.user;

import back.code.admin.dto.user.UserAllInfoDTO;
import back.code.user.entity.UserEntity;
import back.code.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserService {

    private final UserRepository userRepository;

    /** 모든 사용자 정보 조회 */
    @Transactional(readOnly = true)
    public List<UserAllInfoDTO> getAllUsers() {
        List<UserEntity> users = userRepository.findAll();

        return users.stream()
                .map(u -> UserAllInfoDTO.builder()
                        .userId(u.getUserId())
                        .userName(u.getUserName())
                        .userNick(u.getUserNick())
                        .email(u.getEmail())
                        .birth(u.getBirth())
                        .agency(u.getAgency())
                        .phone(u.getPhone())
                        .addr(u.getAddr())
                        .addrDetail(u.getAddrDetail())

                        // 날짜 필드
                        .createAt(u.getCreateAt())
                        .updateAt(u.getUpdateAt())
                        .withdrawAt(u.getWithdrawAt())

                        // 기타 필드
                        .withdrawReason(u.getWithdrawReason())
                        .socialType(u.getSocialType())
                        .status(u.getStatus())

                        // 권한 (UserRoleEntity → roleId)
                        .roleId(u.getUserRole() != null ? u.getUserRole().getRoleId() : null)

                        .build()
                )
                .toList();
    }

    @Transactional
    public void updateUserStatus(String userId, String newStatus) {

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        String oldStatus = user.getStatus();

        // 탈퇴한 사용자는 절대 되돌릴 수 없음
        if (oldStatus.equals("withdrawn") && !newStatus.equals("withdrawn")) {
            throw new IllegalStateException("탈퇴한 사용자는 상태를 변경할 수 없습니다.");
        }

        // 상태 변경 실행
        user.setStatus(newStatus);

        // 정상/정지 상태에서 탈퇴로 변경 시 → 탈퇴일 기록
        if (newStatus.equals("withdrawn") && !oldStatus.equals("withdrawn")) {
            user.setWithdrawAt(Instant.now());
            user.setWithdrawReason("관리자 처리");
        }

        userRepository.save(user);
    }
}
