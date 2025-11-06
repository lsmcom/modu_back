package back.code.accountBook.repository;

import back.code.accountBook.entity.AccountCategoryEntity;
import back.code.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<AccountCategoryEntity, Integer> {

     // 기본 카테고리 조회
     List<AccountCategoryEntity> findAllByIsDefaultTrue();
     // 사용자 정의 카테고리 조회
     List<AccountCategoryEntity> findAllByUser(UserEntity user);

}
