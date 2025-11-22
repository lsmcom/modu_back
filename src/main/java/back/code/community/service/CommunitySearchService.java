package back.code.community.service;

import back.code.common.utils.SecurityUtils;
import back.code.community.dto.CommunitySearchResponse;
import back.code.community.entity.CommunityPostEntity;
import back.code.community.entity.CommunityPostFileEntity;
import back.code.community.repository.CommunityPostFileRepository;
import back.code.community.repository.CommunitySearchRepository;
import back.code.recentsearch.service.RecentSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommunitySearchService {

    private final CommunitySearchRepository searchRepository;
    private final CommunityPostFileRepository postFileRepository;
    private final RecentSearchService recentSearchService;

    @Value("${server.host}")
    private String serverHost;

    // 커뮤니티 검색
    @Transactional(readOnly = true)
    public List<CommunitySearchResponse> searchPosts(String keyword, String range, Integer boardId) {
        String userId = SecurityUtils.getCurrentUserId();

        if (keyword == null || keyword.trim().isEmpty()) {
            throw new RuntimeException("검색어를 입력해주세요.");
        }

        String q = keyword.trim().toLowerCase();

        // 최근 검색어 저장
        try {
            recentSearchService.saveRecentSearch(userId, "COMMUNITY", keyword);
        } catch (Exception e) {
            log.warn("최근 검색어 저장 실패: {}", e.getMessage());
        }

        List<CommunityPostEntity> posts;

        switch (range) {
            case "제목" -> posts = searchRepository.searchByTitle(q, boardId, userId);
            case "작성자" -> posts = searchRepository.searchByUserNick(q, boardId, userId);
            case "글+댓글", "전체" -> posts = searchRepository.searchAll(q, boardId, userId);
            default -> throw new RuntimeException("잘못된 검색 범위입니다.");
        }

        return enrichPostListWithThumbnail(posts);
    }

    /** 게시글 목록에 썸네일 경로 추가 */
    private List<CommunitySearchResponse> enrichPostListWithThumbnail(List<CommunityPostEntity> posts) {
        return posts.stream().map(p -> {
            // 이미지 썸네일
            String thumbnailPath = postFileRepository.findByPost_PostIdOrderByFileOrderAsc(p.getPostId())
                    .stream()
                    .map(CommunityPostFileEntity::getFile)
                    .filter(file -> file.getStoredName() != null &&
                            file.getStoredName().toLowerCase().matches(".*\\.(jpg|jpeg|png|gif|webp)$"))
                    .findFirst()
                    .map(file -> buildUrl(file.getFilePath(), file.getStoredName()))
                    .orElse(null);

            return CommunitySearchResponse.builder()
                    .postId(p.getPostId())
                    .title(p.getTitle())
                    .userNick(p.getUser().getUserNick())
                    .boardName(p.getBoard().getBoardName())
                    .readCount(p.getReadCount())
                    .likeCount(p.getLikeCount())
                    .createAt(String.valueOf(p.getCreateAt()))
                    .thumbnailPath(thumbnailPath)
                    .build();
        }).collect(Collectors.toList());
    }

    /** 공용 파일 URL 생성 유틸 (CommunityPostDTO와 동일한 로직) */
    private String buildUrl(String filePath, String storedName) {
        if (filePath == null || storedName == null) return null;
        String normalized = filePath.replace("\\", "/");
        String relative = normalized.replace("C:/files/modu", "");
        return serverHost + relative + "/" + storedName;
    }
}
