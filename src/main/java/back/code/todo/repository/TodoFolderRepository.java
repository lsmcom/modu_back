package back.code.todo.repository;


import back.code.todo.entity.TodoFolder;
import back.code.todo.entity.TodoFolderId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TodoFolderRepository extends JpaRepository<TodoFolder, TodoFolderId> {

    /**
     * 특정 사용자의 모든 폴더를 조회합니다. (idx_folder_user_id 인덱스 활용)
     * @param userId 사용자 ID
     * @return 해당 사용자가 소유한 TodoFolder 목록
     */
    List<TodoFolder> findByUserId(String userId);
}