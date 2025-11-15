package back.code.todo.repository;


import back.code.todo.entity.TodoFolder;
import back.code.todo.entity.TodoFolderId;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TodoFolderRepository extends JpaRepository<TodoFolder, TodoFolderId> {

    /* 특정 사용자의 모든 폴더를 조회 */
    List<TodoFolder> findByUserId(String userId);

    @Query("SELECT MAX(f.folderId) FROM TodoFolder f WHERE f.userId = :userId")
    Optional<Integer> findMaxFolderIdByUserId(@Param("userId") String userId);

    // 해당 유저의 내역 삭제
    void deleteByUserId(String userId);
}