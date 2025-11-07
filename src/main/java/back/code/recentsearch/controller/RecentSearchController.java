package back.code.recentsearch.controller;

import back.code.recentsearch.dto.RecentSearchDTO;
import back.code.recentsearch.service.RecentSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/recent-search")
public class RecentSearchController {

    private final RecentSearchService recentSearchService;

    /* 최근 검색어 조회 */
    @GetMapping("/{userId}/{type}")
    public ResponseEntity<List<RecentSearchDTO>> getRecentSearches(
            @PathVariable String userId,
            @PathVariable String type) {
        return ResponseEntity.ok(recentSearchService.getRecentSearches(userId, type));
    }

    /* 검색 시 최근 검색어 저장 */
    @PostMapping
    public ResponseEntity<Void> saveRecentSearch(
            @RequestParam String userId,
            @RequestParam String type,
            @RequestParam String keyword) {
        recentSearchService.saveRecentSearch(userId, type, keyword);
        return ResponseEntity.ok().build();
    }

    /* 특정 검색어 삭제 */
    @DeleteMapping
    public ResponseEntity<Void> deleteRecentSearch(
            @RequestParam String userId,
            @RequestParam String type,
            @RequestParam String keyword) {
        recentSearchService.deleteRecentSearch(userId, type, keyword);
        return ResponseEntity.ok().build();
    }

    /* 전체 삭제 */
    @DeleteMapping("/all")
    public ResponseEntity<Void> deleteAllRecentSearches(
            @RequestParam String userId,
            @RequestParam String type) {
        recentSearchService.deleteAllByUserIdAndType(userId, type);
        return ResponseEntity.ok().build();
    }
}
