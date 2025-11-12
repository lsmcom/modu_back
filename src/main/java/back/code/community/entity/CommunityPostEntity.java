package back.code.community.entity;

import back.code.common.entity.BaseTimeEntity;
import back.code.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "community_post")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CommunityPostEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer postId; // 게시글 번호

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = true)
    private CommunityBoardEntity board; // 게시판 테이블 조인

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user; // user 테이블 조인

    private String title; // 게시글 제목

    private String contents; // 게시글 내용

    private Integer readCount; // 게시글 조회수

    private Integer likeCount; // 게시글 추천수

    private Character isTemporary; // 게시글 임시저장 여부

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommunityPostFileEntity> postFiles = new ArrayList<>();

    @OneToOne(mappedBy = "post", fetch = FetchType.LAZY)
    private CommunityPostSettingEntity setting;

    // 생성 전용 팩토리
    public static CommunityPostEntity create(CommunityBoardEntity board, UserEntity user,
            String title, String contents, Character isTemporary) {
        return CommunityPostEntity.builder()
                .board(board)
                .user(user)
                .title(title)
                .contents(contents)
                .readCount(0)
                .likeCount(0)
                .isTemporary(isTemporary == null ? 'N' : isTemporary)
                .build();
    }

    // 조회수 증가 메서드
    public void increaseReadCount() {
        if (readCount == null) readCount = 0;
        this.readCount++;
    }
}
