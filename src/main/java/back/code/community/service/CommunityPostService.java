package back.code.community.service;

import back.code.common.utils.FileUtils;
import back.code.common.utils.SecurityUtils;
import back.code.community.dto.*;
import back.code.community.entity.*;
import back.code.community.entity.enum_.FileRole;
import back.code.community.repository.*;
import back.code.file.entity.FileEntity;
import back.code.file.repository.FileRepository;
import back.code.file.service.FileService;
import back.code.user.entity.UserEntity;
import back.code.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    private final FileUtils fileUtils;
    private final FileRepository fileRepository;
    private final CommunityPostSettingRepository settingRepository;
    private final CommunityPostViewRepository postViewRepository;
    private final CommunityPostLikeRepository postLikeRepository;
    private final CommunityPostCommentRepository commentRepository;
    private final CommunityReportRepository reportRepository;

    /** 댓글 및 이미지 파일 첫번째 부분 가져오기 */
    private void enrichPostListWithExtras(List<CommunityPostDTO> list) {
        for (CommunityPostDTO dto : list) {
            int commentCount = commentRepository.countByPost_PostId(dto.getPostId());
            dto.setCommentCount(commentCount);

            var files = postFileRepository.findByPost_PostIdOrderByFileOrderAsc(dto.getPostId());
            String thumbnailPath = files.stream()
                    .map(CommunityPostFileEntity::getFile)
                    .filter(file -> file.getStoredName() != null &&
                            file.getStoredName().toLowerCase().matches(".*\\.(jpg|jpeg|png|gif|webp)$"))
                    .findFirst()
                    .map(file -> buildUrl(file.getFilePath(), file.getStoredName()))
                    .orElse(null);

            dto.setThumbnailPath(thumbnailPath);
        }
    }

    /** 게시글 전체 목록 조회 */
    @Transactional(readOnly = true)
    public List<CommunityPostDTO> getAllPosts() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        List<CommunityPostDTO> list = communityPostRepository.findAllPostSummariesWithLikeStatus(userId);
        enrichPostListWithExtras(list);
        return list;
    }

    /** 게시판별 게시글 목록 조회 */
    @Transactional(readOnly = true)
    public List<CommunityPostDTO> getPostsByBoardId(Integer boardId) {
        String userId = SecurityUtils.getCurrentUserId();
        List<CommunityPostDTO> list = communityPostRepository.findPostDTOsByBoardId(userId, boardId);
        enrichPostListWithExtras(list);
        return list;
    }

    /** 필독 공지(상단 고정) 조회 */
    @Transactional(readOnly = true)
    public List<CommunityPostDTO> getFixedNotices() {
        return communityFixedNoticeRepository.findFixedNotices();
    }

    /** 게시글 등록, 수정 (임시저장, 파일 업로드 포함) */
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

        CommunityPostEntity post;

        // UPDATE 모드 (임시글 수정 or 최종 등록)
        if (dto.getPostId() != null) {
            post = postRepository.findById(dto.getPostId())
                    .orElseThrow(() -> new RuntimeException("존재하지 않는 게시글입니다."));

            // 제목/내용/게시판/임시여부 갱신
            if (dto.getTitle() != null) post.setTitle(dto.getTitle());
            if (dto.getContents() != null) post.setContents(dto.getContents());
            post.setIsTemporary(Character.valueOf('Y').equals(dto.getIsTemporary()) ? 'Y' : 'N');
            post.setBoard(board);

            // 파일 갱신 로직
            List<String> keepIds = dto.getKeepFileIds() != null ? dto.getKeepFileIds() : new ArrayList<>();
            List<CommunityPostFileEntity> current = postFileRepository.findByPost_PostId(post.getPostId());

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

            // 공지글은 기본적으로 공개 설정 생성 (setting null 방지)
            postSettingRepository.save(
                    CommunityPostSettingEntity.builder()
                            .post(post)
                            .user(user)
                            .isPublic('Y')      // 기본 공개
                            .isSearch('Y')      // 검색 허용
                            .isComment('Y')     // 댓글 허용
                            .isInShare('Y')
                            .isOutShare('Y')
                            .isCopy('Y')
                            .build()
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

        var s = dto.getSetting();
        var existing = postSettingRepository.findByPost_PostId(post.getPostId()).orElse(null);

        if (existing != null) {
            if (s.getIsPublic() != null) existing.setIsPublic(s.getIsPublic().charAt(0));
            if (s.getIsSearch() != null) existing.setIsSearch(s.getIsSearch().charAt(0));
            if (s.getIsComment() != null) existing.setIsComment(s.getIsComment().charAt(0));
            if (s.getIsInShare() != null) existing.setIsInShare(s.getIsInShare().charAt(0));
            if (s.getIsCopy() != null) existing.setIsCopy(s.getIsCopy().charAt(0));
            if (s.getIsOutShare() != null) existing.setIsOutShare(s.getIsOutShare().charAt(0));
            if (s.getImageSizeType() != null) existing.setImageSizeType(s.getImageSizeType());
            postSettingRepository.save(existing);
        } else {
            postSettingRepository.save(
                    CommunityPostSettingEntity.builder()
                            .post(post)
                            .user(user)
                            .isPublic(s.getIsPublic() != null ? s.getIsPublic().charAt(0) : null)
                            .isSearch(s.getIsSearch() != null ? s.getIsSearch().charAt(0) : null)
                            .isComment(s.getIsComment() != null ? s.getIsComment().charAt(0) : null)
                            .isInShare(s.getIsInShare() != null ? s.getIsInShare().charAt(0) : null)
                            .isCopy(s.getIsCopy() != null ? s.getIsCopy().charAt(0) : null)
                            .isOutShare(s.getIsOutShare() != null ? s.getIsOutShare().charAt(0) : null)
                            .imageSizeType(s.getImageSizeType())
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
        List<CommunityPostFileEntity> entities =
                postFileRepository.findByPost_PostIdOrderByFileOrderAsc(postId);

        return entities.stream()
                .map(e -> CommunityPostFileDTO.from(e.getFile()))
                .toList();
    }

    /** 게시글 첨부파일 삭제 */
    @Transactional
    public void deletePostFile(Integer postId, String fileId) {
        try {
            // 삭제 대상 파일 조회
            var mappingOpt = postFileRepository.findByPost_PostIdAndFile_FileId(postId, fileId);
            if (mappingOpt.isEmpty()) {
                log.warn("삭제할 파일 매핑이 없습니다. postId={}, fileId={}", postId, fileId);
                return;
            }
            FileEntity file = mappingOpt.get().getFile();

            // 매핑 삭제
            postFileRepository.deleteByPostIdAndFileIdDirect(postId, fileId);

            // 파일 물리 삭제
            String path = Paths.get(file.getFilePath(), file.getStoredName()).toString();
            fileUtils.deleteFile(path);

            if (file.getFileThumbName() != null) {
                String thumbPath = Paths.get(file.getFilePath(), "thumb", file.getFileThumbName()).toString();
                fileUtils.deleteFile(thumbPath);
            }

            // 파일 삭제 (다른 게시글에서 참조하지 않을 경우)
            long refs = postFileRepository.countByFile_FileId(fileId);
            if (refs == 0) {
                fileRepository.deletePhysicalFile(fileId);
                log.info("[FILE DELETE] 파일 완전 삭제 완료 fileId={}", fileId);
            } else {
                log.info("[FILE DELETE] 다른 참조 존재로 DB 삭제 스킵 fileId={}", fileId);
            }

        } catch (Exception e) {
            log.error("[FILE DELETE] 파일 삭제 중 오류 postId={}, fileId={}", postId, fileId, e);
            throw new RuntimeException("파일 삭제 실패", e);
        }
    }

    // 게시글 삭제(파일 포함)
    @Transactional
    public void deletePost(Integer postId) {
        // 삭제 대상 파일 리스트 가져오기
        var mappings = postFileRepository.findByPost_PostIdWithFile(postId);
        var files = mappings.stream()
                .map(CommunityPostFileEntity::getFile)
                .distinct()
                .toList();

        // 매핑 및 게시글 삭제
        postFileRepository.deleteByPostId(postId);
        postSettingRepository.deleteByPost_PostId(postId);
        communityPostRepository.deleteById(postId);

        // 물리 파일 삭제 및 DB 파일 삭제
        for (FileEntity file : files) {
            try {
                String path = Paths.get(file.getFilePath(), file.getStoredName()).toString();
                fileUtils.deleteFile(path);

                if (file.getFileThumbName() != null) {
                    String thumbPath = Paths.get(file.getFilePath(), "thumb", file.getFileThumbName()).toString();
                    fileUtils.deleteFile(thumbPath);
                }

                long refs = postFileRepository.countByFile_FileId(file.getFileId());
                if (refs == 0) {
                    fileRepository.deletePhysicalFile(file.getFileId());
                    log.info("[POST DELETE] 파일 완전 삭제 완료 fileId={}", file.getFileId());
                }

            } catch (IOException e) {
                log.warn("[POST DELETE] 파일 삭제 실패 fileId={}", file.getFileId(), e);
            }
        }

        log.info("[POST DELETE] 게시글 및 파일 삭제 완료 postId={}", postId);
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

    /** 게시글 상세조회 (조회수 증가 + 프로필 이미지 포함) */
    @Transactional
    public CommunityPostDetailDTO getPostDetail(Integer postId) {

        // 게시글 + 설정 JOIN 조회
        CommunityPostEntity post = postRepository.findPostWithSetting(postId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 게시글입니다."));

        // 현재 로그인한 사용자 ID 가져오기
        String userId = SecurityUtils.getCurrentUserId();

        boolean isLiked = false;
        if (userId != null && !"anonymousUser".equals(userId)) {
            isLiked = postLikeRepository.existsByPost_PostIdAndUser_UserId(postId, userId);
        }

        long realLikeCount = postLikeRepository.countByPost_PostId(postId);

        // 로그인 상태이며 게시글 작성자가 아니고 이전에 조회한 적이 없으면 → 조회수 증가
        if (userId != null && !post.getUser().getUserId().equals(userId)) {
            boolean alreadyViewed = postViewRepository.existsByPost_PostIdAndUser_UserId(postId, userId);
            if (!alreadyViewed) {
                post.increaseReadCount();
                postViewRepository.save(
                        CommunityPostViewEntity.builder()
                                .post(post)
                                .user((UserEntity) userRepository.findByUserId(userId).orElseThrow())
                                .build()
                );
            }
        }

        // 작성자 프로필 이미지 조회 (file_type = 'PROFILE')
        List<FileEntity> profileFiles = fileRepository.findByUser_UserIdAndFileType(
                post.getUser().getUserId(), "PROFILE"
        );

        String profileImagePath = null;
        if (!profileFiles.isEmpty()) {
            FileEntity profileFile = profileFiles.get(0);
            profileImagePath = buildUrl(profileFile.getFilePath(), profileFile.getStoredName());
        }

        // 첨부파일 목록 조회 후 DTO 변환
        List<CommunityPostFileDTO> fileDtos = postFileRepository.findByPost_PostId(postId).stream()
                .map(CommunityPostFileEntity::getFile)
                .map(file -> new CommunityPostFileDTO(
                        file.getFileId(),
                        file.getFileName(),
                        file.getFileSize(),
                        file.getFileType(),
                        file.getFilePath(),
                        file.getStoredName(),
                        file.getFileThumbName(),
                        buildUrl(file.getFilePath(), file.getStoredName())
                ))
                .toList();

        // DTO 변환
        CommunityPostDetailDTO dto = CommunityPostDetailDTO.fromEntity(post, fileDtos, profileImagePath);
        // 실제 좋아요 수 적용
        dto.setLikeCount((int)realLikeCount);
        dto.setLiked(isLiked);
        if (post.getSetting() != null) {
            dto.setImageSizeType(String.valueOf(post.getSetting().getImageSizeType()));
        }

        return dto;
    }

    /** 공용 파일 URL 생성 유틸 */
    private String buildUrl(String filePath, String storedName) {
        if (filePath == null || storedName == null) return null;

        // OS 경로를 표준화
        String normalized = filePath.replace("\\", "/");

        // "C:/files/modu" 제거 → 상대 경로만 남김
        String relative = normalized.replace("C:/files/modu", "");

        // Paths.get()은 중복 / 자동 정리해줌
        String urlPath = Paths.get("files", relative, storedName)
                .toString()
                .replace("\\", "/");

        return "http://localhost:9090/" + urlPath;
    }

    /** 게시글 설정 조회 */
    @Transactional(readOnly = true)
    public CommunityPostSettingDTO getSettingByPostId(Integer postId) {
        CommunityPostSettingEntity entity = settingRepository.findByPost_PostId(postId)
                .orElseThrow(() -> new RuntimeException("게시글 설정 정보를 찾을 수 없습니다."));

        CommunityPostSettingDTO dto = new CommunityPostSettingDTO();
        dto.setIsPublic(String.valueOf(entity.getIsPublic()));
        dto.setIsSearch(String.valueOf(entity.getIsSearch()));
        dto.setIsComment(String.valueOf(entity.getIsComment()));
        dto.setIsInShare(String.valueOf(entity.getIsInShare()));
        dto.setIsOutShare(String.valueOf(entity.getIsOutShare()));
        dto.setIsCopy(String.valueOf(entity.getIsCopy()));
        dto.setImageSizeType(entity.getImageSizeType());
        return dto;
    }

    /** 게시글 추천 (토글 + 결과 반환) */
    @Transactional
    public Map<String, Object> togglePostLike(Integer postId) {
        // 로그인한 사용자 ID 가져오기
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        // 게시글 조회
        CommunityPostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 게시글입니다."));

        // 이미 좋아요한 경우 확인
        Optional<CommunityPostLikeEntity> existingLike =
                postLikeRepository.findByPost_PostIdAndUser_UserId(postId, userId);

        boolean isLiked;

        if (existingLike.isPresent()) {
            // 이미 눌렀으면 좋아요 취소
            postLikeRepository.delete(existingLike.get());
            log.info("[LIKE CANCEL] postId={}, userId={}", postId, userId);
            isLiked = false;
        } else {
            // 처음 누른 경우
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

            CommunityPostLikeEntity newLike = CommunityPostLikeEntity.builder()
                    .post(post)
                    .user(user)
                    .build();

            postLikeRepository.save(newLike);
            log.info("[LIKE ADD] postId={}, userId={}", postId, userId);
            isLiked = true;
        }

        // 총 좋아요 수 다시 계산 후 캐시 필드 동기화
        long likeCount = postLikeRepository.countByPost_PostId(postId);
        post.setLikeCount((int) likeCount);
        postRepository.save(post);

        // 결과 반환
        return Map.of(
                "isLiked", isLiked,
                "likeCount", likeCount
        );
    }

    /** 로그인한 사용자가 특정 게시글을 추천했는지 여부 반환 */
    @Transactional(readOnly = true)
    public boolean isPostLikedByUser(Integer postId) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        if (userId == null || userId.equals("anonymousUser")) {
            return false; // 로그인하지 않은 경우
        }

        return postLikeRepository.existsByPost_PostIdAndUser_UserId(postId, userId);
    }

    /** 사용자 별 게시글, 댓글 수 조회 */
    @Transactional(readOnly = true)
    public Map<String, Integer> getUserPostAndCommentCount(String userId) {
        int postCount = communityPostRepository.countByUser_UserIdAndIsTemporary(userId, 'N');
        int commentCount = commentRepository.countByUser_UserId(userId);

        return Map.of(
                "postCount", postCount,
                "commentCount", commentCount
        );
    }

    /**
     * 인기 게시글 목록 조회
     * - 조회수 / 추천수 / 댓글수 TOP
     * - 기간 필터 (최근 7일 / 최근 30일 / 전체)
     */
    public List<CommunityPostDTO> getPopularPosts(String sortBy, String period) {
        String userId = SecurityUtils.getCurrentUserId();

        LocalDateTime cutoff = switch (period) {
            case "7" -> LocalDateTime.now().minusDays(7);
            case "30" -> LocalDateTime.now().minusDays(30);
            default -> LocalDateTime.now(); // placeholder (사용 안됨)
        };
        return communityPostRepository.findPopularPosts(userId, cutoff, sortBy, period);
    }

    /** 게시글 신고 */
    @Transactional
    public void reportPost(CommunityReportRequest request) {
        // 중복 신고 여부 확인
        if (reportRepository.findByPostIdAndUserId(request.getPostId(), request.getUserId()).isPresent()) {
            throw new RuntimeException("이미 이 게시글을 신고하셨습니다.");
        }

        CommunityReportEntity report = CommunityReportEntity.builder()
                .postId(request.getPostId())
                .userId(request.getUserId())
                .reportReason(request.getReportReason())
                .build();

        reportRepository.save(report);
        log.info("게시글 신고 등록됨 -> postId: {}, userId: {}, reason: {}",
                request.getPostId(), request.getUserId(), request.getReportReason()
        );
    }

    /** 마이페이지 - 내가 쓴 게시글 목록 */
    @Transactional(readOnly = true)
    public List<MyPostActivityDTO> getUserPosts(String userId) {

        // 사용자가 쓴 게시글(임시글 제외, 게시판 포함) 조회
        List<CommunityPostEntity> posts = postRepository.findUserPostsWithBoard(userId);

        // DTO 매핑 + 댓글 수 + 썸네일
        return posts.stream().map(p -> {
            // 댓글 수
            int commentCount = commentRepository.countByPost_PostId(p.getPostId());

            // 썸네일 (첫 번째 이미지 파일)
            String thumbnailPath = postFileRepository.findByPost_PostIdOrderByFileOrderAsc(p.getPostId())
                    .stream()
                    .map(CommunityPostFileEntity::getFile)
                    .filter(file -> file.getStoredName() != null &&
                            file.getStoredName().toLowerCase().matches(".*\\.(jpg|jpeg|png|gif|webp)$"))
                    .findFirst()
                    .map(file -> buildUrl(file.getFilePath(), file.getStoredName()))
                    .orElse(null);

            return MyPostActivityDTO.builder()
                    .postId(p.getPostId())
                    .boardId(p.getBoard() != null ? p.getBoard().getBoardId() : null)
                    .boardName(p.getBoard() != null ? p.getBoard().getBoardName() : null)
                    .title(p.getTitle())
                    .contents(p.getContents())
                    .readCount(p.getReadCount())
                    .likeCount(p.getLikeCount())
                    .commentCount(commentCount)
                    .thumbnailPath(thumbnailPath)
                    .createAt(p.getCreateAt())
                    .build();
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<CommunityPostDTO> getNoticePosts() {
        List<CommunityPostDTO> notices = communityPostRepository.findNoticePosts();
        enrichPostListWithExtras(notices);
        return notices;
    }
}
