package back.code.community.entity;

import back.code.common.entity.BaseCreateTimeEntity;
import back.code.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "community_fixed_notice")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityFixedNoticeEntity {

    @Id
    private Integer fixedId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private CommunityPostEntity post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private UserEntity admin;

    private Byte fixedOrder;

    private LocalDateTime fixedAt;
}
