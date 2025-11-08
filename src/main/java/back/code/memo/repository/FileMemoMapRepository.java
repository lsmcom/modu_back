package back.code.memo.repository;

import back.code.memo.entity.FileMemoMapEntity;
import back.code.memo.entity.FileMemoMapId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FileMemoMapRepository extends JpaRepository<FileMemoMapEntity, FileMemoMapId> {

    // 메모 ID로 연결된 파일 ID 목록 조회
    @Query("SELECT f.memoFile.fileId FROM FileMemoMapEntity f WHERE f.memo.memoId = :memoId")
    List<String> findFileIdsByMemoId(@Param("memoId") Integer memoId);
}
