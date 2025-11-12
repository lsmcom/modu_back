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

     // 해당 유저의 가계부 내역 삭제 (사용자 카테고리만 삭제)
     void deleteByUserAndIsDefaultNull(UserEntity user);

}
