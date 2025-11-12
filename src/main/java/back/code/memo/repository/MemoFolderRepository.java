package back.code.memo.repository;

import back.code.memo.entity.MemoFolderEntity;
import back.code.user.entity.UserEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MemoFolderRepository extends JpaRepository<MemoFolderEntity, Integer> {
    List<MemoFolderEntity> findByUser_UserId(String userId);

    // 해당 유저의 내역 삭제
    void deleteByUser(UserEntity user);
}
