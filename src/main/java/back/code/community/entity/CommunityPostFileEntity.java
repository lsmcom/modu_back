package back.code.community.entity;

import back.code.community.entity.enum_.FileRole;
import back.code.file.entity.FileEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "community_post_file")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@IdClass(CommunityPostFileId.class)
public class CommunityPostFileEntity {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private CommunityPostEntity post;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    private FileEntity file;

    private Integer fileOrder;

    @Enumerated(EnumType.STRING)
    private FileRole fileRole;
}
