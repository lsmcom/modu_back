package back.code.accountBook.repository;

import back.code.accountBook.entity.AccountBookEntity;
import back.code.accountBook.entity.AccountFileMappingEntity;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountFileMappingRepository extends JpaRepository<AccountFileMappingEntity, Integer> {

    // 특정 가계부에 연결된 파일 매핑 조회
    List<AccountFileMappingEntity> findByAccount(AccountBookEntity account);

    // 특정 가계부의 파일 매핑 전체 삭제
    void deleteAllByAccount(AccountBookEntity account);

    // 특정 가계부와 파일 매핑 조회
    @Query(value = """
            select af 
            from AccountFileMappingEntity af 
                join fetch af.file 
            where af.account.accountId = :accountId 
                and af.file.fileId = :fileId
            """)
    Optional<AccountFileMappingEntity> findByAccount_AccountIdAndFile_FileId(@Param("accountId") Integer accountId, 
                                                                            @Param("fileId") String fileId);

    // 특정 가계부의 파일 삭제
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            delete 
            from AccountFileMappingEntity af 
            where af.account.accountId = :accountId 
                and af.file.fileId = :fileId
    """)
    int deleteByAccountIdAndFileIdDirect(@Param("accountId") Integer accountId, 
                                                    @Param("fileId") String fileId);

    // 특정 파일이 다른 가계부에서도 사용 중인지 카운트
    long countByFile_FileId(String fileId);                                                
}
