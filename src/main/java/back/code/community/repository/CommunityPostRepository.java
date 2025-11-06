package back.code.community.repository;

import back.code.community.dto.CommunityPostDTO;
import back.code.community.entity.CommunityPostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommunityPostRepository extends JpaRepository<CommunityPostEntity, Integer> {

    // 게시글 전체 조회 (최신순)
    @Query("""
    SELECT new back.code.community.dto.CommunityPostDTO(
        p.postId, b.boardId, b.boardName, u.userId, u.userNick, f.filePath,
        f.storedName, p.title, p.contents, p.readCount, p.likeCount, p.isTemporary, p.createAt
    )
    FROM CommunityPostEntity p
    JOIN p.board b
    JOIN p.user u
    LEFT JOIN u.files f ON f.fileType = 'PROFILE'
    WHERE p.isTemporary = 'N'
    ORDER BY p.createAt DESC
""")
    List<CommunityPostDTO> findAllPostSummaries();

    // 게시판별 게시글 조회
    @Query("""
    SELECT new back.code.community.dto.CommunityPostDTO(
        p.postId, b.boardId, b.boardName, u.userId, u.userNick, f.filePath,
        f.storedName, p.title, p.contents, p.readCount, p.likeCount, p.isTemporary, p.createAt
    )
    FROM CommunityPostEntity p
    JOIN p.board b
    JOIN p.user u
    LEFT JOIN u.files f ON f.fileType = 'PROFILE'
    WHERE p.isTemporary = 'N'
    AND b.boardId = :boardId
    ORDER BY p.createAt DESC
""")
    List<CommunityPostDTO> findPostDTOsByBoardId(@Param("boardId") Integer boardId);
}
