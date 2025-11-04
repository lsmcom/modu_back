package back.code.memo.repository;

import back.code.memo.entity.FileMemoMapEntity;
import back.code.memo.entity.FileMemoMapId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileMemoMapRepository extends JpaRepository<FileMemoMapEntity, FileMemoMapId> {
}
