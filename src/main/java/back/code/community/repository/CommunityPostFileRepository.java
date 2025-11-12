package back.code.community.repository;

import back.code.community.entity.CommunityPostFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommunityPostFileRepository extends JpaRepository<CommunityPostFileEntity, Integer> {

    // 특정 게시글에 연결된 모든 파일 매핑 조회
    List<CommunityPostFileEntity> findByPost_PostId(Integer postId);

    // 특정 게시글의 파일 중 유지 대상(keepIds)에 포함되지 않은 파일 매핑 삭제
    void deleteByPost_PostIdAndFile_FileIdNotIn(Integer postId, List<String> keepIds);

    // 특정 게시글(postId)에 연결된 모든 파일 매핑 삭제
    void deleteByPost_PostId(Integer postId);

    // 특정 게시글의 첨부파일 목록을 등록 순서(fileOrder) 기준으로 정렬해서 조회
    List<CommunityPostFileEntity> findByPost_PostIdOrderByFileOrderAsc(Integer postId);

    // 특정 게시글(postId)과 파일(fileId)의 매핑 정보를 조회 (파일 정보 즉시 로딩 포함)
    @Query("select cpf from CommunityPostFileEntity cpf join fetch cpf.file where cpf.post.postId = :postId and cpf.file.fileId = :fileId")
    Optional<CommunityPostFileEntity> findByPost_PostIdAndFile_FileId(@Param("postId") Integer postId, @Param("fileId") String fileId);

    // 게시글 내에서 현재 등록된 첨부파일 중 가장 높은 순서(fileOrder) 조회
    @Query("select coalesce(max(pf.fileOrder),0) from CommunityPostFileEntity pf where pf.post.postId = :postId")
    int findMaxOrder(@Param("postId") Integer postId);

    // 특정 파일(fileId)이 다른 게시글에서도 사용 중인지 카운트
    long countByFile_FileId(String fileId);

    // 특정 게시글의 모든 파일 매핑을 한 번에 삭제 (벌크 연산)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from CommunityPostFileEntity cpf where cpf.post.postId = :postId")
    int deleteByPostId(@Param("postId") Integer postId);

    // 특정 게시글(postId)에 연결된 특정 파일(fileId)만 매핑에서 제거
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from CommunityPostFileEntity cpf where cpf.post.postId = :postId and cpf.file.fileId = :fileId")
    int deleteByPostIdAndFileIdDirect(@Param("postId") Integer postId, @Param("fileId") String fileId);

    // 특정 게시글(postId)에 연결된 파일 매핑 + 파일 정보를 함께 조회 (fetch join)
    @Query("""
        SELECT pf FROM CommunityPostFileEntity pf
        JOIN FETCH pf.file f
        WHERE pf.post.postId = :postId
    """)
    List<CommunityPostFileEntity> findByPost_PostIdWithFile(@Param("postId") Integer postId);
}
