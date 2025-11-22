package back.code.community.repository;

import back.code.community.dto.CommunityPostDTO;
import back.code.community.entity.CommunityFixedNoticeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommunityFixedNoticeRepository extends JpaRepository<CommunityFixedNoticeEntity, Integer> {

    /** 필독 공지(상단 고정) 목록 조회 */
    @Query("""
        SELECT new back.code.community.dto.CommunityPostDTO(
            p.postId, b.boardId, b.boardName, u.userId, u.userNick, f.storedName,
            p.title, p.contents, p.readCount, p.likeCount, p.isTemporary, p.createAt
        )
        FROM CommunityFixedNoticeEntity fn
        JOIN fn.post p
        JOIN p.board b
        JOIN p.user u
        LEFT JOIN u.files f ON f.fileType = 'PROFILE'
        WHERE p.isTemporary = 'N'
        ORDER BY fn.fixedOrder ASC
    """)
    List<CommunityPostDTO> findFixedNotices();
}
