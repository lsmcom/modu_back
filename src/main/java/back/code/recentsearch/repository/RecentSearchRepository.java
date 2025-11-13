package back.code.recentsearch.repository;

import back.code.recentsearch.entity.RecentSearchEntity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecentSearchRepository extends JpaRepository<RecentSearchEntity, Long> {

    Optional<RecentSearchEntity> findByUserIdAndTypeAndKeyword(String userId, String type, String keyword);

    List<RecentSearchEntity> findTop10ByUserIdAndTypeOrderByIdDesc(String userId, String type);

    /* 특정 유저 + 타입 전체 삭제용 */
    void deleteAllByUserIdAndType(String userId, String type);

    // 해당 유저의 내역 삭제
    void deleteByUserId(String userId);
}
