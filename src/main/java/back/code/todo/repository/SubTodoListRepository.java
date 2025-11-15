package back.code.todo.repository;

import back.code.todo.entity.SubTodoList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubTodoListRepository extends JpaRepository<SubTodoList, Integer> {

    /* 특정 상위 Todo 항목(todoListId)에 속하는 모든 SubTodoList 항목을 조회 */
    List<SubTodoList> findByTodoList_TodoId(Integer todoListId);

    /* 특정 상위 Todo 항목(todoListId)에 속하는 모든 SubTodoList 항목을 삭제 */
    void deleteByTodoList_TodoId(Integer todoListId);
}