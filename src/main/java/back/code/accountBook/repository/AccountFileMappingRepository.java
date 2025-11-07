package back.code.accountBook.repository;

import back.code.accountBook.entity.AccountBookEntity;
import back.code.accountBook.entity.AccountFileMappingEntity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountFileMappingRepository extends JpaRepository<AccountFileMappingEntity, Integer> {

    // 특정 가계부에 연결된 파일 매핑 조회
    List<AccountFileMappingEntity> findByAccount(AccountBookEntity account);

    // 특정 가계부의 파일 매핑 전체 삭제
    void deleteAllByAccount(AccountBookEntity account);

}
