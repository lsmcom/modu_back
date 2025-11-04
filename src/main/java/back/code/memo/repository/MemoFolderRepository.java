package back.code.memo.repository;

import back.code.memo.entity.MemoFolderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MemoFolderRepository extends JpaRepository<MemoFolderEntity, Integer> {
    List<MemoFolderEntity> findByUser_UserId(String userId);
}
