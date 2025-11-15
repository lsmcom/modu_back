package back.code.admin.service.user;

import back.code.admin.dto.user.UserAllInfoDTO;
import back.code.user.entity.UserEntity;
import back.code.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
