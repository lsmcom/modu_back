package back.code.recentsearch.service;

import back.code.recentsearch.dto.RecentSearchDTO;
import back.code.recentsearch.entity.RecentSearchEntity;
import back.code.recentsearch.repository.RecentSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecentSearchService {

    private final RecentSearchRepository recentSearchRepository;

    /* 최근 검색어 저장 (이미 있으면 무시) */
    @Transactional
    public void saveRecentSearch(String userId, String type, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return;

        boolean exists = recentSearchRepository
                .findByUserIdAndTypeAndKeyword(userId, type, keyword)
                .isPresent();

        if (!exists) {
            RecentSearchEntity newSearch = RecentSearchEntity.builder()
                    .userId(userId)
                    .type(type)
                    .keyword(keyword)
                    .build();
            recentSearchRepository.save(newSearch);
        }
    }

    /* 최근 검색어 조회 (최신순 10개) */
    @Transactional(readOnly = true)
    public List<RecentSearchDTO> getRecentSearches(String userId, String type) {
        return recentSearchRepository.findTop10ByUserIdAndTypeOrderByIdDesc(userId, type)
                .stream()
                .map(RecentSearchDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /* 특정 검색어 삭제 */
    @Transactional
    public void deleteRecentSearch(String userId, String type, String keyword) {
        recentSearchRepository.findByUserIdAndTypeAndKeyword(userId, type, keyword)
                .ifPresent(recentSearchRepository::delete);
    }

    /* 전체 삭제 (유저 + 타입 기준) */
    @Transactional
    public void deleteAllByUserIdAndType(String userId, String type) {
        recentSearchRepository.deleteAllByUserIdAndType(userId, type);
    }
}
