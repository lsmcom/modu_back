package back.code.memo.repository;

import back.code.memo.entity.MemoFolderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemoFolderRepository extends JpaRepository<MemoFolderEntity, Integer> {
}
