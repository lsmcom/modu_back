package back.code.todo.repository;

import back.code.todo.entity.TodoCompletionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Repository;

@Repository
public interface TodoCompletionStatusRepository extends JpaRepository<TodoCompletionStatus, String> {

    /**
     * 특정 사용자의 완료 카운트를 1 증가시키거나, 레코드가 없으면 새로 생성합니다.
     * MySQL의 INSERT ... ON DUPLICATE KEY UPDATE 구문을 사용하여 원자적으로 처리합니다.
     */
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO todo_completion_status (user_id, completed_count) " +
            "VALUES (:userId, 1) " +
            "ON DUPLICATE KEY UPDATE completed_count = completed_count + 1",
            nativeQuery = true)
    void incrementCompletionCount(@Param("userId") String userId);

    /**
     * 완료 카운트 조회 메소드
     */
    @Query("SELECT t.completedCount FROM TodoCompletionStatus t WHERE t.userId = :userId")
    Integer getCompletedCount(@Param("userId") String userId);

    /**
     * 최초 사용자라면 레코드 생성이 필요한 경우를 위해 존재 여부 체크
     */
    boolean existsByUserId(String userId);
}