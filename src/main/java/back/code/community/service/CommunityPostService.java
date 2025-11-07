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
import back.code.file.service.FileService;
import back.code.user.entity.UserEntity;
import back.code.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommunityPostService {

    private final CommunityPostRepository communityPostRepository;
    private final CommunityFixedNoticeRepository communityFixedNoticeRepository;
    private final CommunityPostRepository postRepository;
    private final CommunityPostFileRepository postFileRepository;
    private final CommunityPostSettingRepository postSettingRepository;
    private final UserRepository userRepository;
    private final FileService fileService;
    private final CommunityBoardRepository communityBoardRepository;

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

        // 공지 권한 체크는 동일
        if (dto.getBoardId() != null && "공지사항".equals(board.getBoardName())
                && !"ROLE_ADMIN".equals(user.getUserRole().getRoleId())) {
            throw new RuntimeException("공지사항은 관리자만 작성 가능합니다.");
        }

        // 유효성
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

        if (dto.getPostId() != null) {
            // 업데이트 모드 (임시/최종 모두)
            post = postRepository.findById(dto.getPostId())
                    .orElseThrow(() -> new RuntimeException("존재하지 않는 게시글입니다."));
            // 소유자 체크 등 필요 시 추가

            // 제목/내용/게시판/임시여부 갱신
            if (dto.getTitle() != null) post.setTitle(dto.getTitle());
            if (dto.getContents() != null) post.setContents(dto.getContents());
            if (!isTemp) { // 최종 등록이면 게시판 필수
                if (dto.getBoardId() == null) throw new RuntimeException("게시판 선택은 필수입니다.");
                post.setBoard(board);
            } else {
                // 임시저장은 board null 허용
                post.setBoard(board); // null 가능
            }
            post.setIsTemporary(isTemp ? 'Y' : 'N');

            // 첨부 갱신 정책 적용
            List<CommunityPostFileEntity> current = postFileRepository.findByPost_PostId(post.getPostId());

            if (Boolean.TRUE.equals(dto.getReplaceAll())) {
                // 전부 삭제 후 새로 매핑
                postFileRepository.deleteByPost_PostId(post.getPostId());
                current.clear();
            } else if (dto.getKeepFileIds() != null) {
                // keep 외 매핑 삭제
                if (dto.getKeepFileIds().isEmpty()) {
                    postFileRepository.deleteByPost_PostId(post.getPostId());
                    current.clear();
                } else {
                    postFileRepository.deleteByPost_PostIdAndFile_FileIdNotIn(post.getPostId(), dto.getKeepFileIds());
                    // current는 굳이 재조회 안 해도 순번 계산만 주의
                    current = postFileRepository.findByPost_PostId(post.getPostId());
                }
            }

            // 새로 올라온 파일만 업로드/매핑
            if (files != null && !files.isEmpty()) {
                int order = current.size() + 1;
                List<CommunityPostFileEntity> mappings = new ArrayList<>();
                for (MultipartFile f : files) {
                    FileEntity fe = fileService.uploadFileAndReturnEntity(f, user.getUserId(), "POST");
                    mappings.add(CommunityPostFileEntity.builder()
                            .post(post)
                            .file(fe)
                            .fileOrder(order++)
                            .fileRole(FileRole.ATTACHMENT)
                            .build());
                }
                postFileRepository.saveAll(mappings);
            }

            // 설정 upsert
            if (dto.getSetting() != null) {
                postSettingRepository.save(
                        CommunityPostSettingEntity.builder()
                                .post(post).user(user)
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
            return post.getPostId();
        } else {
            // 최초 생성 모드
            post = postRepository.save(
                    CommunityPostEntity.create(board, user, dto.getTitle(), dto.getContents(), dto.getIsTemporary())
            );

            if (files != null && !files.isEmpty()) {
                int order = 1;
                List<CommunityPostFileEntity> mappings = new ArrayList<>();
                for (MultipartFile f : files) {
                    FileEntity fe = fileService.uploadFileAndReturnEntity(f, user.getUserId(), "POST");
                    mappings.add(CommunityPostFileEntity.builder()
                            .post(post)
                            .file(fe)
                            .fileOrder(order++)
                            .fileRole(FileRole.ATTACHMENT)
                            .build());
                }
                postFileRepository.saveAll(mappings);
            }

            if (dto.getSetting() != null) {
                postSettingRepository.save(
                        CommunityPostSettingEntity.builder()
                                .post(post).user(user)
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
            return post.getPostId();
        }
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
    public void deletePostFile(Integer postId, String fileId) throws IOException {
        CommunityPostFileEntity mapping = postFileRepository
                .findByPost_PostIdAndFile_FileId(postId, fileId)
                .orElseThrow(() -> new RuntimeException("첨부파일 매핑이 존재하지 않습니다."));

        // 매핑 삭제
        postFileRepository.delete(mapping);

        // 실제 파일도 삭제
        fileService.deleteFileEntity(mapping.getFile());
    }
}
