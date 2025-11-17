package back.code.inquiry.service;

import back.code.common.utils.FileUtils;
import back.code.common.utils.SecurityUtils;
import back.code.file.entity.FileEntity;
import back.code.file.repository.FileRepository;
import back.code.file.service.FileService;
import back.code.inquiry.dto.*;
import back.code.inquiry.entity.InquiryEntity;
import back.code.inquiry.entity.InquiryFileMappingEntity;
import back.code.inquiry.entity.InquiryStatus;
import back.code.inquiry.repository.InquiryFileMappingRepository;
import back.code.inquiry.repository.InquiryRepository;
import back.code.user.entity.UserEntity;
import back.code.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final InquiryFileMappingRepository inquiryFileMappingRepository;
    private final UserRepository userRepository;
    private final FileService fileService;
    private final FileUtils fileUtils;
    private final FileRepository fileRepository;

    @PersistenceContext
    private EntityManager em;

    /** 전체 문의사항 조회 (최신순) */
    @Transactional(readOnly = true)
    public List<InquiryDto> getAllInquiries() {

        List<InquiryEntity> inquiries = inquiryRepository.findAllByOrderByCreateAtDesc();

        return inquiries.stream()
                .map(InquiryDto::fromEntity)
                .toList();
    }

    /**
     * 문의사항 등록 (파일 첨부 포함)
     * - 일반 사용자: 항상 비공개(isPublic = false)
     * - 관리자: 공개 문의(공지/FAQ) 작성 가능
     */
    @Transactional
    public Long createInquiry(InquiryCreateRequest dto, List<MultipartFile> files) throws IOException {

        // 작성자 조회
        UserEntity user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("유효하지 않은 사용자입니다."));

        // 공개 여부(관리자만 true 허용)
        boolean isPublic = "ROLE_ADMIN".equals(user.getUserRole().getRoleId());

        if (isPublic) {
            throw new RuntimeException("일반 사용자는 공개 문의를 작성할 수 없습니다.");
        }

        // 제목/내용 검증
        if (dto.getTitle() == null || dto.getTitle().isBlank() ||
                dto.getContent() == null || dto.getContent().isBlank()) {
            throw new RuntimeException("제목과 내용은 필수입니다.");
        }

        // 상태 기본값 처리 (null이면 submitted)
        String status = dto.getStatus();
        if (status == null || status.isBlank()) {
            status = "submitted";
        }

        // 문의 엔티티 생성 및 저장
        InquiryEntity inquiry = InquiryEntity.builder()
                .user(user)
                .title(dto.getTitle())
                .content(dto.getContent())
                .status(InquiryStatus.valueOf(status))
                .isPublic(isPublic)
                .build();

        inquiryRepository.save(inquiry);

        // 파일 업로드 + 매핑 저장
        if (files != null && !files.isEmpty()) {
            for (MultipartFile mf : files) {
                // 공용 FileService 사용 (file_type = "INQUIRY")
                FileEntity fileEntity = fileService.uploadFileAndReturnEntity(mf, user.getUserId(), "INQUIRY");

                InquiryFileMappingEntity mapping = InquiryFileMappingEntity.builder()
                        .inquiry(inquiry)
                        .file(fileEntity)
                        .build();

                inquiryFileMappingRepository.save(mapping);
            }
        }

        log.info("[INQUIRY CREATE] inquiryId={}, userId={}, isPublic={}, status={}",
                inquiry.getInquiryId(), user.getUserId(), isPublic, status);

        return inquiry.getInquiryId();
    }

    /** 문의사항 상세 조회 */
    @Transactional(readOnly = true)
    public InquiryDetailResponse getInquiryDetail(Long inquiryId) {

        String userId = SecurityUtils.getCurrentUserId();
        String roleId = SecurityUtils.getCurrentUserRole();

        InquiryEntity inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 문의입니다."));

        boolean isAdmin = "ROLE_ADMIN".equals(roleId);
        boolean isOwner = inquiry.getUser().getUserId().equals(userId);

        // 접근 제한 : 비공개 글은 본인 또는 관리자만 가능
        if (!inquiry.isPublic() && !isAdmin && !isOwner) {
            throw new RuntimeException("해당 문의글에 대한 조회 권한이 없습니다.");
        }

        // 첨부파일 조회
        List<InquiryFileMappingEntity> files = inquiryFileMappingRepository.findByInquiry_InquiryId(inquiryId);

        // 작성자 프로필 이미지 조회 (file_type = 'PROFILE')
        List<FileEntity> profileFiles = fileRepository.findByUser_UserIdAndFileType(
                inquiry.getUser().getUserId(), "PROFILE"
        );

        String profileImagePath = null;
        if (!profileFiles.isEmpty()) {
            FileEntity profileFile = profileFiles.get(0);
            profileImagePath = buildUrl(profileFile.getFilePath(), profileFile.getStoredName());
        }

        return InquiryDetailResponse.builder()
                .inquiryId(inquiry.getInquiryId())
                .userId(inquiry.getUser().getUserId())
                .userNick(inquiry.getUser().getUserNick())
                .profileImagePath(profileImagePath)
                .title(inquiry.getTitle())
                .content(inquiry.getContent())
                .createAt(inquiry.getCreateAt())
                .isPublic(inquiry.isPublic())
                .status(String.valueOf(inquiry.getStatus()))
                .files(files.stream().map(file -> new InquiryDetailResponse.FileDto(
                        file.getFile().getFileId(),
                        file.getFile().getFileName(),
                        file.getFile().getStoredName(),
                        file.getFile().getFilePath()
                )).toList())
                .build();
    }

    /** 파일 삭제 */
    @Transactional
    public void deleteInquiryFile(Long inquiryId, String fileId) {
        try {
            // 매핑 조회 + 파일까지 fetch join
            InquiryFileMappingEntity mapping = inquiryFileMappingRepository
                    .findByInquiryIdAndFileId(inquiryId, fileId)
                    .orElseThrow(() -> new RuntimeException("삭제할 파일이 없습니다."));

            FileEntity file = mapping.getFile();

            // 매핑 삭제
            inquiryFileMappingRepository.delete(mapping);

            // 물리 파일 삭제
            String fullPath = Paths.get(file.getFilePath(), file.getStoredName()).toString();
            fileUtils.deleteFile(fullPath);

            if (file.getFileThumbName() != null) {
                String thumbPath = Paths.get(file.getFilePath(), "thumb", file.getFileThumbName()).toString();
                fileUtils.deleteFile(thumbPath);
            }

            // 파일 엔티티 DB 삭제 (참조 없을 때만)
            long refs = inquiryFileMappingRepository.countByFile_FileId(fileId);
            if (refs == 0) {
                fileRepository.deletePhysicalFile(fileId);
            }

            log.info("[INQUIRY FILE DELETE] inquiryId={}, fileId={}", inquiryId, fileId);

        } catch (Exception e) {
            log.error("[INQUIRY FILE DELETE ERROR] inquiryId={}, fileId={}", inquiryId, fileId, e);
            throw new RuntimeException("파일 삭제 실패");
        }
    }

    /** 문의사항 게시글 수정 */
    @Transactional
    public Long updateInquiry(InquiryUpdateRequest dto,
                              List<MultipartFile> newFiles) throws IOException {

        String userId = SecurityUtils.getCurrentUserId();
        String roleId = SecurityUtils.getCurrentUserRole();

        InquiryEntity inquiry = inquiryRepository.findById(dto.getInquiryId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 문의입니다."));

        boolean isAdmin = "ROLE_ADMIN".equals(roleId);
        boolean isOwner = inquiry.getUser().getUserId().equals(userId);

        if (!isAdmin && !isOwner) {
            throw new RuntimeException("수정 권한이 없습니다.");
        }

        // 제목/내용 수정
        inquiry.setTitle(dto.getTitle());
        inquiry.setContent(dto.getContent());

        // 새 파일 업로드
        if (newFiles != null && !newFiles.isEmpty()) {
            for (MultipartFile mf : newFiles) {
                FileEntity fe = fileService.uploadFileAndReturnEntity(
                        mf,
                        inquiry.getUser().getUserId(),
                        "INQUIRY"
                );

                InquiryFileMappingEntity mapping = InquiryFileMappingEntity.builder()
                        .inquiry(inquiry)
                        .file(fe)
                        .build();

                inquiryFileMappingRepository.save(mapping);
            }
        }

        return inquiry.getInquiryId();
    }

    /** 문의사항 게시글 삭제 (파일 포함) */
    @Transactional
    public void deleteInquiry(Long inquiryId) {

        // 로그인 사용자 확인
        String userId = SecurityUtils.getCurrentUserId();
        String roleId = SecurityUtils.getCurrentUserRole();

        // 대상 조회
        InquiryEntity inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 문의입니다."));

        boolean isAdmin = "ROLE_ADMIN".equals(roleId);
        boolean isOwner = inquiry.getUser().getUserId().equals(userId);

        // 권한 체크
        if (!isAdmin && !isOwner) {
            throw new RuntimeException("삭제 권한이 없습니다.");
        }

        // 삭제할 매핑 + 파일 조회 (fetch join)
        List<InquiryFileMappingEntity> mappings =
                inquiryFileMappingRepository.findByInquiryIdWithFile(inquiryId);

        // 파일 리스트 추출 (중복 제거)
        List<FileEntity> files = mappings.stream()
                .map(InquiryFileMappingEntity::getFile)
                .distinct()
                .toList();

        // 매핑 삭제
        inquiryFileMappingRepository.deleteByInquiry_InquiryId(inquiryId);

        // 문의글 삭제
        inquiryRepository.delete(inquiry);

        // 파일 물리 삭제 + DB 삭제
        for (FileEntity file : files) {
            try {
                // 물리 파일 삭제
                String path = Paths.get(file.getFilePath(), file.getStoredName()).toString();
                fileUtils.deleteFile(path);

                if (file.getFileThumbName() != null) {
                    String thumbPath = Paths.get(file.getFilePath(), "thumb", file.getFileThumbName()).toString();
                    fileUtils.deleteFile(thumbPath);
                }

                // 매핑 참조 수 확인 후 DB 파일 삭제
                long refs = inquiryFileMappingRepository.countByFile_FileId(file.getFileId());
                if (refs == 0) {
                    fileRepository.deletePhysicalFile(file.getFileId());
                }

            } catch (IOException e) {
                log.warn("[INQUIRY DELETE] 파일 삭제 실패 fileId={}", file.getFileId(), e);
            }
        }

        log.info("[INQUIRY DELETE] 문의글 + 파일 삭제 완료 inquiryId={}", inquiryId);
    }

    /** 사용자 별 문의사항 게시글 수 조회 */
    @Transactional(readOnly = true)
    public int getUserInquiryCount(String userId) {
        int inquiryCount = inquiryRepository.countByUser_UserId(userId);

        return inquiryCount;
    }

    // 사용자별 문의 목록 조회
    @Transactional(readOnly = true)
    public List<MyInquiryListDTO> getUserInquiries(String userId) {

        List<InquiryEntity> list =
                inquiryRepository.findByUser_UserIdOrderByCreateAtDesc(userId);

        return list.stream()
                .map(i -> MyInquiryListDTO.builder()
                        .inquiryId(i.getInquiryId())
                        .title(i.getTitle())
                        .status(i.getStatus().name()) // submitted, answered, temp
                        .build()
                ).toList();
    }

    /** 공용 파일 URL 생성 유틸 (CommunityPostDTO와 동일한 로직) */
    private String buildUrl(String filePath, String storedName) {
        if (filePath == null || storedName == null) return null;
        String normalized = filePath.replace("\\", "/");
        String relative = normalized.replace("C:/files/modu", "");
        return "http://localhost:9090" + relative + "/" + storedName;
    }
}
