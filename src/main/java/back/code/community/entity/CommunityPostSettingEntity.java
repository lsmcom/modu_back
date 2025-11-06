package back.code.community.entity;

import back.code.community.entity.enum_.ImageSizeType;
import back.code.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "community_post_setting")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CommunityPostSettingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer settingId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private CommunityPostEntity post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    private Character isPublic;
    private Character isSearch;
    private Character isComment;
    private Character isInShare;
    private Character isCopy;
    private Character isOutShare;

    @Enumerated(EnumType.STRING)
    private ImageSizeType imageSizeType;
}
