package back.code.community.controller;

import back.code.common.dto.ApiResponse;
import back.code.community.dto.CommunityPostDTO;
import back.code.community.service.CommunityPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/community")
public class CommunityPostController {

    private final CommunityPostService communityPostService;

    /** 게시글 전체 목록 조회 */
    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<List<CommunityPostDTO>>> getAllPosts() {
        List<CommunityPostDTO> postList = communityPostService.getAllPosts();
        return ResponseEntity.ok(ApiResponse.ok(postList));
    }

    /** 게시판별 게시글 목록 조회 */
    @GetMapping("/boards/{boardId}/posts")
    public ResponseEntity<ApiResponse<List<CommunityPostDTO>>> getPostsByBoard(@PathVariable Integer boardId) {
        List<CommunityPostDTO> posts = communityPostService.getPostsByBoardId(boardId);
        return ResponseEntity.ok(ApiResponse.ok(posts));
    }

    /** 필독 공지(상단 고정) 조회 */
    @GetMapping("/notices/fixed")
    public ResponseEntity<ApiResponse<List<CommunityPostDTO>>> getFixedNotices() {
        List<CommunityPostDTO> fixedNotices = communityPostService.getFixedNotices();
        return ResponseEntity.ok(ApiResponse.ok(fixedNotices));
    }
}
