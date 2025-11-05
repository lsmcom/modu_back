package back.code.community.entity;

import back.code.common.entity.BaseTimeEntity;
import back.code.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "community_post")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityPostEntity extends BaseTimeEntity {

    @Id
    private Integer postId; // 게시글 번호

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private CommunityBoardEntity board; // 게시판 테이블 조인

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user; // user 테이블 조인

    private String title; // 게시글 제목

    private String contents; // 게시글 내용

    private Integer readCount; // 게시글 조회수

    private Integer likeCount; // 게시글 추천수

    private Character isTemporary; // 게시글 임시저장 여부
}
