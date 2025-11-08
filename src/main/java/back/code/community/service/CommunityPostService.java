package back.code.community.service;

import back.code.community.dto.CommunityPostCreateDTO;
import back.code.community.dto.CommunityPostDTO;
import back.code.community.dto.CommunityPostFileDTO;
import back.code.community.entity.CommunityBoardEntity;
import back.code.community.entity.CommunityPostEntity;
import back.code.community.entity.CommunityPostFileEntity;
import back.code.community.entity.CommunityPostSettingEntity;
import back.code.community.entity.enum_.FileRole;
import back.code.community.repository.*;
import back.code.file.entity.FileEntity;
import back.code.file.event.OrphanFileCleanupEvent;
import back.code.file.service.FileService;
import back.code.user.entity.UserEntity;
import back.code.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommunityPostService {

    private final CommunityPostRepository communityPostRepository;
    private final CommunityFixedNoticeRepository communityFixedNoticeRepository;
    private final CommunityPostRepository postRepository;
    private final CommunityPostFileRepository postFileRepository;
    private final CommunityPostSettingRepository postSettingRepository;
    private final UserRepository userRepository;
    private final FileService fileService;
    private final CommunityBoardRepository communityBoardRepository;
    private final ApplicationEventPublisher eventPublisher;

    /** 게시글 전체 목록 조회 */
    @Transactional(readOnly = true)
    public List<CommunityPostDTO> getAllPosts() {
        return communityPostRepository.findAllPostSummaries();
    }

    /** 게시판별 게시글 목록 조회 */
    @Transactional(readOnly = true)
    public List<CommunityPostDTO> getPostsByBoardId(Integer boardId) {
        return communityPostRepository.findPostDTOsByBoardId(boardId);
    }

    /** 필독 공지(상단 고정) 조회 */
    @Transactional(readOnly = true)
    public List<CommunityPostDTO> getFixedNotices() {
        return communityFixedNoticeRepository.findFixedNotices();
    }

    /** 게시글 등록 (임시저장, 파일 업로드 포함) */
    @Transactional
    public Integer createPost(CommunityPostCreateDTO dto, List<MultipartFile> files) throws IOException {

        UserEntity user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

        CommunityBoardEntity board = (dto.getBoardId() != null)
                ? communityBoardRepository.findById(dto.getBoardId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 게시판입니다."))
                : null;

        // 공지 권한 체크
        if (dto.getBoardId() != null && "공지사항".equals(board.getBoardName())
                && !"ROLE_ADMIN".equals(user.getUserRole().getRoleId())) {
            throw new RuntimeException("공지사항은 관리자만 작성 가능합니다.");
        }

        // 유효성 검사
        if (dto.getIsTemporary() == 'Y') {
            if ((dto.getTitle() == null || dto.getTitle().isBlank()) &&
                    (dto.getContents() == null || dto.getContents().isBlank())) {
                throw new RuntimeException("임시저장은 제목 또는 내용 중 하나가 필요합니다.");
            }
        } else {
            if (dto.getTitle() == null || dto.getTitle().isBlank() ||
                    dto.getContents() == null || dto.getContents().isBlank()) {
                throw new RuntimeException("제목과 내용은 필수입니다.");
            }
            if (dto.getBoardId() == null) {
                throw new RuntimeException("게시판 선택은 필수입니다.");
            }
        }

        final boolean isTemp = Character.valueOf('Y').equals(dto.getIsTemporary());

        CommunityPostEntity post;

        // UPDATE 모드 (임시글 수정 or 최종 등록)
        if (dto.getPostId() != null) {
            post = postRepository.findById(dto.getPostId())
                    .orElseThrow(() -> new RuntimeException("존재하지 않는 게시글입니다."));

            // 제목/내용/게시판/임시여부 갱신
            if (dto.getTitle() != null) post.setTitle(dto.getTitle());
            if (dto.getContents() != null) post.setContents(dto.getContents());
            post.setIsTemporary(isTemp ? 'Y' : 'N');
            post.setBoard(board);

            // 파일 갱신 로직
            List<String> keepIds = dto.getKeepFileIds() != null ? dto.getKeepFileIds() : new ArrayList<>();

            List<CommunityPostFileEntity> current = postFileRepository.findByPost_PostId(post.getPostId());

            // keep 목록에 없는 매핑 삭제
            if (keepIds.isEmpty()) {
                postFileRepository.deleteByPost_PostId(post.getPostId());
                current.clear();
            } else {
                postFileRepository.deleteByPost_PostIdAndFile_FileIdNotIn(post.getPostId(), keepIds);
                current = postFileRepository.findByPost_PostId(post.getPostId());
            }

            // 새 파일 업로드
            if (files != null && !files.isEmpty()) {
                int order = current.size() + 1;
                for (MultipartFile f : files) {
                    FileEntity fe = fileService.uploadFileAndReturnEntity(f, user.getUserId(), "POST");

                    CommunityPostFileEntity mapping = CommunityPostFileEntity.builder()
                            .post(post)
                            .file(fe)
                            .fileOrder(order++)
                            .fileRole(FileRole.ATTACHMENT)
                            .build();

                    postFileRepository.save(mapping);
                }
            }

            postRepository.save(post);
        }

        // CREATE 모드 (최초 저장)
        else {
            post = postRepository.save(
                    CommunityPostEntity.create(board, user, dto.getTitle(), dto.getContents(), dto.getIsTemporary())
            );

            // 파일 업로드
            if (files != null && !files.isEmpty()) {
                int order = 1;
                for (MultipartFile f : files) {
                    FileEntity fe = fileService.uploadFileAndReturnEntity(f, user.getUserId(), "POST");

                    CommunityPostFileEntity mapping = CommunityPostFileEntity.builder()
                            .post(post)
                            .file(fe)
                            .fileOrder(order++)
                            .fileRole(FileRole.ATTACHMENT)
                            .build();

                    postFileRepository.save(mapping);
                }
            }
        }

        // 공통 설정 저장 (Upsert)
        saveOrUpdateSetting(dto, user, post);

        return post.getPostId();
    }

    /** 게시글 설정 Upsert (기존 있으면 수정, 없으면 새로 추가) */
    private void saveOrUpdateSetting(CommunityPostCreateDTO dto, UserEntity user, CommunityPostEntity post) {
        if (dto.getSetting() == null) return;

        var existing = postSettingRepository.findByPost_PostId(post.getPostId()).orElse(null);

        if (existing != null) {
            existing.setIsPublic(dto.getSetting().getIsPublic());
            existing.setIsSearch(dto.getSetting().getIsSearch());
            existing.setIsComment(dto.getSetting().getIsComment());
            existing.setIsInShare(dto.getSetting().getIsInShare());
            existing.setIsCopy(dto.getSetting().getIsCopy());
            existing.setIsOutShare(dto.getSetting().getIsOutShare());
            existing.setImageSizeType(dto.getSetting().getImageSizeType());
            postSettingRepository.save(existing);
        } else {
            postSettingRepository.save(
                    CommunityPostSettingEntity.builder()
                            .post(post)
                            .user(user)
                            .isPublic(dto.getSetting().getIsPublic())
                            .isSearch(dto.getSetting().getIsSearch())
                            .isComment(dto.getSetting().getIsComment())
                            .isInShare(dto.getSetting().getIsInShare())
                            .isCopy(dto.getSetting().getIsCopy())
                            .isOutShare(dto.getSetting().getIsOutShare())
                            .imageSizeType(dto.getSetting().getImageSizeType())
                            .build()
            );
        }
    }

    /** 사용자별 임시저장 게시글 조회 */
    @Transactional(readOnly = true)
    public List<CommunityPostDTO> getTempPosts(String userId) {
        return communityPostRepository.findTempPostsByUserId(userId);
    }

    /** 게시글 첨부파일 조회 */
    @Transactional(readOnly = true)
    public List<CommunityPostFileDTO> getPostFiles(Integer postId) {
        List<CommunityPostFileEntity> entities = postFileRepository.findByPost_PostIdOrderByFileOrderAsc(postId);

        return entities.stream()
                .map(e -> {
                    var f = e.getFile();
                    return new CommunityPostFileDTO(
                            f.getFileId(),
                            f.getFileName(),
                            f.getFileSize(),
                            f.getFileType(),
                            f.getFilePath(),
                            f.getStoredName(),
                            f.getFileThumbName()
                    );
                })
                .toList();
    }

    /** 게시글 첨부파일 삭제 */
    @Transactional
    public void deletePostFile(Integer postId, String fileId) {
        try {
            // 매핑 삭제
            int deleted = postFileRepository.deleteByPostIdAndFileIdDirect(postId, fileId);
            if (deleted == 0) {
                log.warn("매핑이 존재하지 않거나 이미 삭제된 상태입니다. postId={}, fileId={}", postId, fileId);
                return;
            }

            // 파일이 다른 게시글에서도 참조 중인지 확인
            long stillUsed = postFileRepository.countByFile_FileId(fileId);
            if (stillUsed == 0) {
                // 참조 끊겼으면 고아 파일 정리 이벤트 발행
                eventPublisher.publishEvent(new OrphanFileCleanupEvent(List.of(fileId)));
                log.info("게시글 파일 매핑 + 고아 파일 정리 완료 postId={}, fileId={}", postId, fileId);
            } else {
                log.info("게시글 파일 매핑만 삭제 (다른 참조 남음) postId={}, fileId={}", postId, fileId);
            }

        } catch (Exception e) {
            log.error("게시글 파일 매핑 삭제 중 오류 발생", e);
            throw new RuntimeException("파일 매핑 삭제 실패", e);
        }
    }

    // 게시글 삭제(파일 포함)
    @Transactional
    public void deletePost(Integer postId) {
        // 삭제 후보 파일ID 모으기
        var mappings = postFileRepository.findByPost_PostId(postId);
        var candidateFileIds = mappings.stream()
                .map(m -> m.getFile().getFileId())
                .distinct()
                .toList();

        // 매핑 벌크 삭제
        postFileRepository.deleteByPostId(postId);

        // 설정 벌크 삭제
        postSettingRepository.deleteByPost_PostId(postId);

        // 게시글 삭제
        communityPostRepository.deleteById(postId);

        // 커밋 후 파일 고아 정리 이벤트 발행
        eventPublisher.publishEvent(new OrphanFileCleanupEvent(candidateFileIds));
    }

    /** 60일 지난 임시글 자동 삭제 */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void deleteOldTemporaryPosts() throws IOException {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(60);
        List<Integer> oldPostIds = communityPostRepository.findOldTemporaryPostIds(cutoff);
        if (oldPostIds.isEmpty()) return;

        for (Integer postId : oldPostIds) {
            try {
                deletePost(postId);
            } catch (Exception e) {
                log.warn("[AUTO CLEANUP] 임시글 삭제 실패 postId={}", postId, e);
            }
        }
        log.info("[AUTO CLEANUP] {}개의 오래된 임시글 자동 삭제 완료", oldPostIds.size());
    }
}
