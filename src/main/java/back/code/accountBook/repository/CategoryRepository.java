package back.code.accountBook.repository;

import back.code.accountBook.entity.AccountCategoryEntity;
import back.code.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<AccountCategoryEntity, Integer> {

     // 기본 카테고리 조회
     List<AccountCategoryEntity> findByUserAndIsDefaultTrue(UserEntity user);

     // 사용자 정의 카테고리 조회
     List<AccountCategoryEntity> findByUserAndIsDefaultFalseOrIsDefaultIsNull(UserEntity user);

     // 특정 유저의 모든 카테고리 조회
     List<AccountCategoryEntity> findAllByUser(UserEntity user);

     // 특정 유저의 모든 카테고리 삭제 (초기화용)
     void deleteByUser(UserEntity user);

     // 특정 유저의 사용자 정의 카테고리만 삭제
     void deleteByUserAndIsDefaultFalseOrIsDefaultIsNull(UserEntity user);

     // 특정 유저의 기본 카테고리 중복 체크
     boolean existsByUserAndCategoryNameAndIsDefaultTrue(UserEntity user, String categoryName);

}
