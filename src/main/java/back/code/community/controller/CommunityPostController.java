package back.code.community.controller;

import back.code.common.dto.ApiResponse;
import back.code.community.dto.CommunityPostCreateDTO;
import back.code.community.dto.CommunityPostDTO;
import back.code.community.dto.CommunityPostFileDTO;
import back.code.community.service.CommunityPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    public ResponseEntity<ApiResponse<String>> deletePostFile(
            @PathVariable Integer postId,
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
}
