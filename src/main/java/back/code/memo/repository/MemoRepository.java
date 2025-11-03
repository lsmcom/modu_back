package back.code.memo.repository;

import back.code.memo.entity.MemoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface MemoRepository extends JpaRepository<MemoEntity, Integer> {

    @Query("SELECT m FROM MemoEntity m WHERE m.user.userId = :userId AND m.folder.folderId = :folderId")
    List<MemoEntity> findByUserAndFolder(@Param("userId") String userId, @Param("folderId") Integer folderId);

    @Query("SELECT m FROM MemoEntity m JOIN FETCH m.folder WHERE m.user.userId = :userId")
    List<MemoEntity> findAllByUserIdWithFolder(@Param("userId") String userId);

}
