package back.code.user.entity;

import back.code.common.entity.BaseTimeEntity;
import back.code.file.entity.FileEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "user")
public class UserEntity extends BaseTimeEntity {

    @Id
    private String userId; //회원 아이디

    private String password; //회원 비밀번호

    private String userName; //회원 이름

    private String userNick; //회원 닉네임

    private String email; //회원 이메일

    private LocalDate birth; //회원 생년월일

    private String agency; //회원 통신사

    private String phone; //회원 전화번호

    private String addr; //회원 주소

    private String addrDetail; //회원 상세주소

    private Instant withdrawAt; //회원 탈퇴일

    private String withdrawReason; //회원 탈퇴이유

    private String socialType; //회원 소셜 종류(naver, kakao)

    private String status; //회원 상태(ex: 활성화, 정지, 휴먼)

    @ManyToOne
    @JoinColumn(name = "user_role")
    private UserRoleEntity userRole; //회원 권한

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<FileEntity> files = new ArrayList<>();

}