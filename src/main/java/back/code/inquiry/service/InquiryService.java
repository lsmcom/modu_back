package back.code.inquiry.service;

import back.code.file.entity.FileEntity;
import back.code.file.service.FileService;
import back.code.inquiry.dto.InquiryCreateRequest;
import back.code.inquiry.dto.InquiryDto;
import back.code.inquiry.entity.InquiryEntity;
import back.code.inquiry.entity.InquiryFileMappingEntity;
import back.code.inquiry.entity.InquiryStatus;
import back.code.inquiry.repository.InquiryFileMappingRepository;
import back.code.inquiry.repository.InquiryRepository;
import back.code.user.entity.UserEntity;
import back.code.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final InquiryFileMappingRepository inquiryFileMappingRepository;
    private final UserRepository userRepository;
    private final FileService fileService;

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
        boolean isPublic = "ADMIN".equals(user.getUserRole().getRoleId());

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
}
