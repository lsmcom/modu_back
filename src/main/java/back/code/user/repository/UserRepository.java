package back.code.user.repository;

import back.code.user.dto.LoginUserInfoDTO;
import back.code.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * 사용자 정보를 조회하는 JPA Repository.
 */
public interface UserRepository extends JpaRepository<UserEntity, String> {

    // 로그인 시 필요한 최소 컬럼만 조회하기 위해 DTO Projection을 사용
    @Query("SELECT new back.code.user.dto.LoginUserInfoDTO(u.userId, u.userName, u.password, r.roleName, u.status) " +
            "FROM UserEntity u JOIN u.userRole r " +
            "WHERE u.userId = :userId")
    Optional<LoginUserInfoDTO> findLoginInfo(@Param("userId") String userId);

    // 닉네임으로 사용자 찾기
    Optional<UserEntity> findByUserNick(String userNick);

    // 이메일로 사용자 찾기
    Optional<UserEntity> findByEmail(String email);

    // 하이픈 제거 후 전화번호로 사용자 찾기
    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN 1 ELSE 0 END FROM user WHERE REPLACE(phone, '-', '') = :normalizedPhone", nativeQuery = true)
    int existsByPhoneNormalized(@Param("normalizedPhone") String normalizedPhone);

    // 닉네임 중복 확인용
    boolean existsByUserNick(String userNick);
}
