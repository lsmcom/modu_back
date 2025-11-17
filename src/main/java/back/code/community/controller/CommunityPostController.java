package back.code.community.controller;

import back.code.common.dto.ApiResponse;
import back.code.community.dto.*;
import back.code.community.service.CommunityPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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

    /** 게시글 등록 (임시저장, 파일 업로드 포함) */
    @PostMapping(value = "/posts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Integer>> createPost(@RequestPart("meta") CommunityPostCreateDTO meta,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) throws IOException {
        Integer postId = communityPostService.createPost(meta, files);
        return ResponseEntity.ok(ApiResponse.ok(postId));
    }

    /** 사용자별 임시저장 게시글 조회 */
    @GetMapping("/posts/temp/{userId}")
    public ResponseEntity<ApiResponse<List<CommunityPostDTO>>> getTempPosts(@PathVariable String userId) {
        List<CommunityPostDTO> temps = communityPostService.getTempPosts(userId);
        return ResponseEntity.ok(ApiResponse.ok(temps));
    }

    /** 게시글 첨부파일 조회 */
    @GetMapping("/posts/{postId}/files")
    public ResponseEntity<ApiResponse<List<CommunityPostFileDTO>>> getPostFiles(@PathVariable Integer postId) {
        List<CommunityPostFileDTO> files = communityPostService.getPostFiles(postId);
        return ResponseEntity.ok(ApiResponse.ok(files));
    }

    /** 게시글 첨부파일 삭제 */
    @DeleteMapping("/posts/{postId}/files/{fileId}")
    public ResponseEntity<ApiResponse<String>> deletePostFile(@PathVariable Integer postId,
            @PathVariable String fileId) throws IOException {
        communityPostService.deletePostFile(postId, fileId);
        return ResponseEntity.ok(ApiResponse.ok("삭제 완료"));
    }

    /** 게시글 삭제 (임시글 포함) */
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<ApiResponse<String>> deletePost(@PathVariable Integer postId) throws IOException {
        communityPostService.deletePost(postId);
        return ResponseEntity.ok(ApiResponse.ok("삭제 완료"));
    }

    /** 게시글 상세조회 */
    @GetMapping("/posts/{postId}")
    public ResponseEntity<ApiResponse<CommunityPostDetailDTO>> getPostDetail(@PathVariable Integer postId) {
        CommunityPostDetailDTO dto = communityPostService.getPostDetail(postId);
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    /**
     * 게시글 설정 조회 (postId 기준)
     * - 수정 페이지 진입 시 기존 설정값 불러오기용
     */
    @GetMapping("/setting/{postId}")
    public ResponseEntity<ApiResponse<CommunityPostSettingDTO>> getSetting(@PathVariable Integer postId) {
        CommunityPostSettingDTO dto = communityPostService.getSettingByPostId(postId);
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    /** 게시글 추천 */
    @PatchMapping("/posts/{postId}/like")
    public ResponseEntity<ApiResponse<Map<String, Object>>> toggleLike(@PathVariable Integer postId) {
        Map<String, Object> result = communityPostService.togglePostLike(postId);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /** 게시글 추천 상태 확인 */
    @GetMapping("/posts/{postId}/like-status")
    public ResponseEntity<ApiResponse<Boolean>> checkLikeStatus(@PathVariable Integer postId) {
        boolean liked = communityPostService.isPostLikedByUser(postId);
        return ResponseEntity.ok(ApiResponse.ok(liked));
    }

    /** 사용자 별 게시글, 댓글 수 조회 */
    @GetMapping("/users/{userId}/activity-count")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> getUserActivityCount(@PathVariable String userId) {
        Map<String, Integer> result = communityPostService.getUserPostAndCommentCount(userId);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /**
     * 인기 게시글 조회
     *
     * - sortBy : 정렬 기준 ("view" = 조회수, "like" = 추천수, "comment" = 댓글수)
     * - period : 기간 필터 ("7" = 최근 7일, "30" = 최근 30일, "all" = 전체)
     */
    @GetMapping("/posts/popular")
    public ResponseEntity<ApiResponse<List<CommunityPostDTO>>> getPopularPosts(
            @RequestParam(defaultValue = "view") String sortBy, @RequestParam(defaultValue = "7") String period) {
        List<CommunityPostDTO> posts = communityPostService.getPopularPosts(sortBy, period);
        return ResponseEntity.ok(ApiResponse.ok(posts));
    }

    /** 게시글 신고 */
    @PostMapping("/report")
    public ResponseEntity<ApiResponse<String>> reportPost(@RequestBody CommunityReportRequest request) {
        communityPostService.reportPost(request);
        return ResponseEntity.ok(ApiResponse.ok("신고가 접수되었습니다."));
    }

    /** 마이페이지 - 내가 쓴 게시글 목록 조회 */
    @GetMapping("/users/{userId}/posts")
    public ResponseEntity<ApiResponse<List<MyPostActivityDTO>>> getUserPosts(@PathVariable String userId) {
        List<MyPostActivityDTO> posts = communityPostService.getUserPosts(userId);
        return ResponseEntity.ok(ApiResponse.ok(posts));
    }
}
