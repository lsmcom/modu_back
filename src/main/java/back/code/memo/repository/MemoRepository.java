package back.code.memo.repository;

import back.code.memo.entity.MemoEntity;
import back.code.user.entity.UserEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface MemoRepository extends JpaRepository<MemoEntity, Integer> {

    @Query("SELECT m FROM MemoEntity m WHERE m.user.userId = :userId AND m.folder.folderId = :folderId")
    List<MemoEntity> findByUserAndFolder(@Param("userId") String userId, @Param("folderId") Integer folderId);

    @Query("SELECT m FROM MemoEntity m JOIN FETCH m.folder WHERE m.user.userId = :userId")
    List<MemoEntity> findAllByUserIdWithFolder(@Param("userId") String userId);

    /* 단일 메모 + 폴더 + 사용자 포함 조회 */
    @Query("SELECT m FROM MemoEntity m " +
            "LEFT JOIN FETCH m.folder " +
            "LEFT JOIN FETCH m.user " +
            "WHERE m.memoId = :memoId")
    Optional<MemoEntity> findByIdWithFolderAndUser(@Param("memoId") Integer memoId);

    //메모 검색
    @Query("SELECT m FROM MemoEntity m WHERE m.user.userId = :userId AND LOWER(m.memoTitle) LIKE LOWER(:keyword)")
    List<MemoEntity> findByUserIdAndTitleContainingIgnoreCase(@Param("userId") String userId, @Param("keyword") String keyword);

    @Query("SELECT m FROM MemoEntity m WHERE m.user.userId = :userId AND LOWER(m.memoContents) LIKE LOWER(:keyword)")
    List<MemoEntity> findByUserIdAndContentsContainingIgnoreCase(@Param("userId") String userId, @Param("keyword") String keyword);

    @Query("SELECT m FROM MemoEntity m WHERE m.user.userId = :userId AND (LOWER(m.memoTitle) LIKE LOWER(:keyword) OR LOWER(m.memoContents) LIKE LOWER(:keyword))")
    List<MemoEntity> findByUserIdAndKeyword(@Param("userId") String userId, @Param("keyword") String keyword);

    // 해당 유저의 내역 삭제
    void deleteByUser(UserEntity user);

}
