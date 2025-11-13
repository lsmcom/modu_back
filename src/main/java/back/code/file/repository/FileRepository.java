package back.code.file.repository;

import back.code.file.entity.FileEntity;
import back.code.user.entity.UserEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FileRepository extends JpaRepository<FileEntity, String> {

    // 해당 유저의 파일리스트 찾기
    List<FileEntity> findByUser_UserId(String userId);

    // 파일 종류별 파일리스트 찾기
    List<FileEntity> findByFileType(String fileType);

    // 해당 유저가 가지고 있는 파일들 중 특정 종류의 파일리스트 찾기
    List<FileEntity> findByUser_UserIdAndFileType(String userId, String fileType);

    // 해당 유저가 특정 타입의 파일을 가지고 있는지 검사
    @Query(value = "SELECT EXISTS(SELECT 1 FROM file WHERE user_id = :userId AND file_type = :fileType)", nativeQuery = true)
    int existsProfileFile(@Param("userId") String userId, @Param("fileType") String fileType);

    @Modifying
    @Query(value = "DELETE FROM file WHERE file_id = :fileId", nativeQuery = true)
    void deletePhysicalFile(@Param("fileId") String fileId);

    // 해당 유저의 내역 삭제
    void deleteByUser(UserEntity user);

}
