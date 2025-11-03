package back.code.user.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "user_role")
public class UserRoleEntity {

    @Id
    private String roleId; //권한 아이디
    private String roleName; //권한 이름
}