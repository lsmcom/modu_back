package back.code.todo.repository;

import back.code.todo.entity.TodoList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

@Repository
public interface TodoListRepository extends JpaRepository<TodoList, Integer> {

    List<TodoList> findByFolder_UserIdOrderByOrderIndexAsc(String userId);

    List<TodoList> findByFolder_FolderIdOrderByOrderIndexAsc(Integer folderId);

    List<TodoList> findByFolder_UserIdAndFolder_FolderIdOrderByOrderIndexAsc(String userId, Integer folderId);

    List<TodoList> findByFolder_UserIdAndTdFixedOrderByOrderIndexAsc(String userId, Boolean tdFixed);

    List<TodoList> findByFolder_UserIdAndIsCompletedOrderByOrderIndexAsc(String userId, Boolean isCompleted);

    List<TodoList> findByFolder_UserIdAndIsCompletedAndDueDateBefore(String userId, Boolean isCompleted, LocalDateTime now);

    @Query("SELECT MAX(t.orderIndex) FROM TodoList t WHERE t.folder.userId = :userId")
    Optional<Integer> findMaxOrderIndexByUserId(@Param("userId") String userId);

    // Native 쿼리는 DB 컬럼 이름을 사용하므로 수정 불필요
    @Query(value = "SELECT IFNULL(SUM(t.completed_count), 0) FROM todo_completion_status t WHERE t.user_id = :userId", nativeQuery = true)
    long countCompletedTodosByUserId(@Param("userId") String userId);
}