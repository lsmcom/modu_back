package back.code.community.controller;

import back.code.common.dto.ApiResponse;
import back.code.community.dto.CommunityPostCommentCreateDTO;
import back.code.community.dto.CommunityPostCommentDTO;
import back.code.community.service.CommunityPostCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/community")
public class CommunityPostCommentController {

    private final CommunityPostCommentService commentService;

    /** 게시글별 댓글/대댓글 목록 */
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<List<CommunityPostCommentDTO>>> getComments(@PathVariable Integer postId) {
        List<CommunityPostCommentDTO> comments = commentService.getCommentsByPostId(postId);
        return ResponseEntity.ok(ApiResponse.ok(comments));
    }

    /** 댓글/대댓글 생성 */
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<Integer>> createComment(@PathVariable Integer postId,
            @RequestBody CommunityPostCommentCreateDTO dto) {
        dto.setPostId(postId);
        Integer commentId = commentService.createComment(dto);
        return ResponseEntity.ok(ApiResponse.ok(commentId));
    }

    /** 댓글 수정 */
    @PatchMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<String>> updateComment(@PathVariable Integer commentId,
            @RequestParam String contents) {
        commentService.updateComment(commentId, contents);
        return ResponseEntity.ok(ApiResponse.ok("댓글 수정 완료"));
    }

    /** 댓글 삭제 */
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<String>> deleteComment(@PathVariable Integer commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.ok(ApiResponse.ok("댓글 삭제 완료"));
    }
}
