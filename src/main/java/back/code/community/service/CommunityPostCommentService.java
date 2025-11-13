package back.code.community.service;

import back.code.common.utils.SecurityUtils;
import back.code.community.dto.CommunityPostCommentCreateDTO;
import back.code.community.dto.CommunityPostCommentDTO;
import back.code.community.entity.CommunityPostCommentEntity;
import back.code.community.entity.CommunityPostEntity;
import back.code.community.repository.CommunityPostCommentRepository;
import back.code.community.repository.CommunityPostRepository;
import back.code.file.repository.FileRepository;
import back.code.user.entity.UserEntity;
import back.code.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommunityPostCommentService {

    private final CommunityPostCommentRepository commentRepository;
    private final CommunityPostRepository postRepository;
    private final UserRepository userRepository;
    private final FileRepository fileRepository;

    /** 댓글/대댓글 등록 */
    @Transactional
    public Integer createComment(CommunityPostCommentCreateDTO dto) {
        String userId = SecurityUtils.getCurrentUserId();

        // 로그인 사용자 확인
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("로그인 정보를 확인할 수 없습니다."));

        // 게시글 확인
        CommunityPostEntity post = postRepository.findById(dto.getPostId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 게시글입니다."));

        // 글쓰기 설정에서 댓글 비허용일 경우
        if (post.getSetting() != null && post.getSetting().getIsComment() == 'N') {
            throw new RuntimeException("이 게시글은 댓글 작성을 허용하지 않습니다.");
        }

        // 부모 댓글 (대댓글일 경우)
        CommunityPostCommentEntity parent = null;
        if (dto.getParentCommentId() != null) {
            parent = commentRepository.findById(dto.getParentCommentId())
                    .orElseThrow(() -> new RuntimeException("존재하지 않는 부모 댓글입니다."));
        }

        CommunityPostCommentEntity comment = CommunityPostCommentEntity.builder()
                .post(post)
                .user(user)
                .contents(dto.getContents())
                .parentComment(parent)
                .build();

        commentRepository.save(comment);

        log.info("[COMMENT CREATE] postId={}, userId={}, parentId={}", dto.getPostId(), userId, dto.getParentCommentId());

        return comment.getCommentId();
    }

    /** 댓글 수정 */
    @Transactional
    public void updateComment(Integer commentId, String newContents) {
        if (newContents == null || newContents.isBlank()) {
            throw new RuntimeException("댓글 내용은 비워둘 수 없습니다.");
        }

        String userId = SecurityUtils.getCurrentUserId();

        CommunityPostCommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 댓글입니다."));

        if (!comment.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("본인이 작성한 댓글만 수정할 수 있습니다.");
        }

        comment.setContents(newContents);
        log.info("[COMMENT UPDATE] commentId={}, userId={}", commentId, userId);
    }

    /** 댓글 삭제 */
    @Transactional
    public void deleteComment(Integer commentId) {
        String userId = SecurityUtils.getCurrentUserId();

        CommunityPostCommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 댓글입니다."));

        if (!comment.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("본인이 작성한 댓글만 삭제할 수 있습니다.");
        }

        commentRepository.delete(comment);
        log.info("[COMMENT DELETE] commentId={}, userId={}", commentId, userId);
    }

    /** 게시글별 댓글/대댓글 전체 조회 */
    @Transactional(readOnly = true)
    public List<CommunityPostCommentDTO> getCommentsByPostId(Integer postId) {
        List<CommunityPostCommentEntity> all = commentRepository.findByPost_PostIdOrderByCreateAtAsc(postId);

        // 최상위 댓글만 뽑고, 재귀적으로 대댓글 DTO 구성
        return all.stream()
                .filter(c -> c.getParentComment() == null)
                .map(c -> CommunityPostCommentDTO.fromEntityWithReplies(c, fileRepository))
                .collect(Collectors.toList());
    }
}
