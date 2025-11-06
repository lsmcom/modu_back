package back.code.todo.repository;

import back.code.todo.entity.TodoList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.time.Instant;

@Repository
public interface TodoListRepository extends JpaRepository<TodoList, Integer> {

    /**
     * 특정 사용자의 모든 할 일을 order_index 오름차순으로 조회.
     * @param userId 사용자 ID
     * @return 해당 사용자의 정렬된 TodoList 목록
     */
    List<TodoList> findByUserIdOrderByOrderIndexAsc(String userId);

    /**
     * 특정 폴더에 속하는 모든 할 일을 order_index 오름차순으로 조회.
     * @param folderId 폴더 ID
     * @return 해당 폴더의 정렬된 TodoList 목록
     */
    List<TodoList> findByFolderIdOrderByOrderIndexAsc(Integer folderId);

    /**
     * 특정 사용자의 특정 폴더에 속하는 모든 할 일을 조회. (idx_user_folder_order 인덱스 활용)
     * @param userId 사용자 ID
     * @param folderId 폴더 ID
     * @return 해당 조건의 TodoList 목록
     */
    List<TodoList> findByUserIdAndFolderIdOrderByOrderIndexAsc(String userId, Integer folderId);

    /**
     * 특정 사용자의 고정(Fixed)된 할 일만 order_index 오름차순으로 조회.
     * @param userId 사용자 ID
     * @param tdFixed 고정 상태 (true)
     * @return 고정된 TodoList 목록
     */
    List<TodoList> findByUserIdAndTdFixedOrderByOrderIndexAsc(String userId, Boolean tdFixed);

    /**
     * 특정 사용자의 완료되지 않은(미완료) 할 일을 order_index 오름차순으로 조회.
     * @param userId 사용자 ID
     * @param isCompleted 완료 상태 (false)
     * @return 미완료 TodoList 목록
     */
    List<TodoList> findByUserIdAndIsCompletedOrderByOrderIndexAsc(String userId, Boolean isCompleted);

    /**
     * 특정 사용자의 미완료 항목 중 기한이 초과된 항목을 조회.
     * @param userId 사용자 ID
     * @param now 현재 시간 기준
     * @return 기한 초과 TodoList 목록
     */
    List<TodoList> findByUserIdAndIsCompletedAndDueDateBefore(String userId, Boolean isCompleted, Instant now);

    /**
     * 특정 사용자가 가진 Todo 항목 중 가장 큰 orderIndex를 조회합니다.
     * @param userId 사용자 ID
     * @return 최대 orderIndex (Optional)
     */
    Optional<Integer> findTopByUserIdOrderByOrderIndexDesc(String userId);
}
