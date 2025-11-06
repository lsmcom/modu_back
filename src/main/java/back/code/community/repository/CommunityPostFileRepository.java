package back.code.community.repository;

import back.code.community.entity.CommunityPostFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommunityPostFileRepository extends JpaRepository<CommunityPostFileEntity, Integer> {

    List<CommunityPostFileEntity> findByPost_PostId(Integer postId);
    void deleteByPost_PostIdAndFile_FileIdNotIn(Integer postId, List<String> keepIds);
    void deleteByPost_PostId(Integer postId);

    List<CommunityPostFileEntity> findByPost_PostIdOrderByFileOrderAsc(Integer postId);

    Optional<CommunityPostFileEntity> findByPost_PostIdAndFile_FileId(Integer postId, String fileId);

    @Query("select coalesce(max(pf.fileOrder),0) from CommunityPostFileEntity pf where pf.post.postId = :postId")
    int findMaxOrder(@Param("postId") Integer postId);
}
