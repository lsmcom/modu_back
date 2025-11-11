package back.code.community.repository;

import back.code.community.entity.CommunityPostFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommunityPostFileRepository extends JpaRepository<CommunityPostFileEntity, Integer> {

    List<CommunityPostFileEntity> findByPost_PostId(Integer postId);

    void deleteByPost_PostIdAndFile_FileIdNotIn(Integer postId, List<String> keepIds);

    void deleteByPost_PostId(Integer postId);

    List<CommunityPostFileEntity> findByPost_PostIdOrderByFileOrderAsc(Integer postId);

    @Query("select cpf from CommunityPostFileEntity cpf join fetch cpf.file where cpf.post.postId = :postId and cpf.file.fileId = :fileId")
    Optional<CommunityPostFileEntity> findByPost_PostIdAndFile_FileId(@Param("postId") Integer postId, @Param("fileId") String fileId);

    @Query("select coalesce(max(pf.fileOrder),0) from CommunityPostFileEntity pf where pf.post.postId = :postId")
    int findMaxOrder(@Param("postId") Integer postId);

    long countByFile_FileId(String fileId);

    // 게시글 단위 매핑 삭제 (벌크)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from CommunityPostFileEntity cpf where cpf.post.postId = :postId")
    int deleteByPostId(@Param("postId") Integer postId);

    // 게시글-파일 단일 매핑 삭제
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from CommunityPostFileEntity cpf where cpf.post.postId = :postId and cpf.file.fileId = :fileId")
    int deleteByPostIdAndFileIdDirect(@Param("postId") Integer postId, @Param("fileId") String fileId);
}
