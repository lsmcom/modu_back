package back.code.inquiry.service;

import back.code.inquiry.repository.InquiryRepository; // 4. 코드 내 수정된 부분을 명확히 표시: 파일 경로 수정
import back.code.inquiry.dto.*; // 4. 코드 내 수정된 부분을 명확히 표시: 파일 경로 수정
import back.code.inquiry.entity.Inquiry; // 4. 코드 내 수정된 부분을 명확히 표시: 파일 경로 수정
import back.code.inquiry.repository.InquiryReplyRepository; // 4. 코드 내 수정된 부분을 명확히 표시: 파일 경로 수정
import back.code.user.repository.UserRepository;
import back.code.user.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

// 4. 코드 내 수정된 부분을 명확히 표시: Inquiry 서비스 (import 경로 수정)
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final InquiryReplyRepository replyRepository;
    private final UserRepository userRepository;

    /**
     * 문의 목록 조회 (Inquiry.jsx)
     */
    public List<InquiryListResponseDto> getInquiryList(String userId, Pageable pageable) {
        // 1. 공지사항 및 FAQ (isPublic=true)
        List<InquiryListResponseDto> publicInquiries = inquiryRepository.findByIsPublicTrueOrderByCreateAtDesc()
                .stream()
                .map(inquiry -> {
                    String type = inquiry.getInquiryId() < 200 ? "notice" : "faq";
                    return InquiryListResponseDto.fromEntity(inquiry, type, 1L);
                })
                .collect(Collectors.toList());

        // 2. 비공개 문의 (isPublic=false, 현재 user가 작성한 것만)
        String sortType = pageable.getSort().stream()
                .findFirst()
                .map(order -> order.getProperty().equals("createAt") && order.isDescending() ? "latest" : "created")
                .orElse("latest");

        Sort sort = sortType.equals("latest") ? Sort.by(Sort.Direction.DESC, "createAt") : Sort.by(Sort.Direction.ASC, "createAt");
        Pageable privatePageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        Page<Inquiry> privateInquiryPage = inquiryRepository.findByIsPublicFalseAndUser_UserId(userId, privatePageable);

        List<InquiryListResponseDto> privateInquiries = privateInquiryPage.getContent()
                .stream()
                .map(inquiry -> {
                    Long replyCount = replyRepository.countByInquiry_InquiryId(inquiry.getInquiryId());
                    return InquiryListResponseDto.fromEntity(inquiry, null, replyCount);
                })
                .collect(Collectors.toList());

        // 3. 공지/FAQ + 비공개 문의 통합
        List<InquiryListResponseDto> result = new java.util.ArrayList<>();
        result.addAll(publicInquiries);
        result.addAll(privateInquiries);

        return result;
    }

    /**
     * 문의 상세 조회 (InquiryDetail.jsx)
     */
    public InquiryDetailResponseDto getInquiryDetail(Long inquiryId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("문의글을 찾을 수 없습니다: " + inquiryId));

        Long replyCount = replyRepository.countByInquiry_InquiryId(inquiryId);
        List<InquiryReplyResponseDto> replies = replyRepository.findByInquiry_InquiryIdOrderByCreateAtAsc(inquiryId) // 4. 코드 내 수정된 부분을 명확히 표시: RepliedAt -> CreateAt으로 수정
                .stream()
                .map(InquiryReplyResponseDto::fromEntity)
                .collect(Collectors.toList());

        return InquiryDetailResponseDto.fromEntity(inquiry, replyCount, replies);
    }

    /**
     * 문의 등록 (WriteForm.jsx)
     */
    @Transactional
    public InquiryDetailResponseDto createInquiry(InquirySaveRequestDto requestDto) {
        UserEntity user = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + requestDto.getUserId()));

        Inquiry inquiry = requestDto.toEntity(user);
        inquiry = inquiryRepository.save(inquiry);

        return getInquiryDetail(inquiry.getInquiryId());
    }

    /**
     * 문의 수정 (WriteForm.jsx)
     */
    @Transactional
    public InquiryDetailResponseDto updateInquiry(Long inquiryId, InquirySaveRequestDto requestDto) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("문의글을 찾을 수 없습니다: " + inquiryId));

        inquiry.setTitle(requestDto.getTitle());
        inquiry.setContent(requestDto.getContent());
        inquiry.setIsPublic(requestDto.getIsPublic());
        inquiry.setStatus(requestDto.getStatus());

        return getInquiryDetail(inquiryId);
    }

    /**
     * 문의 삭제 (InquiryDetail.jsx)
     */
    @Transactional
    public void deleteInquiry(Long inquiryId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("문의글을 찾을 수 없습니다: " + inquiryId));

        inquiryRepository.delete(inquiry);
    }

    // 4. 코드 내 수정된 부분을 명확히 표시: 문의 검색 기능 (InquirySearch.jsx)
    public Page<InquiryListResponseDto> searchInquiries(String userId, String keyword, String range, Pageable pageable) {
        Page<Inquiry> inquiryPage;

        // 검색 범위에 따른 레포지토리 메서드 호출
        switch (range) {
            case "제목":
                inquiryPage = inquiryRepository.findByIsPublicFalseAndUser_UserIdAndTitleContainingIgnoreCase(userId, keyword, pageable);
                break;
            case "작성자":
                inquiryPage = inquiryRepository.findByIsPublicFalseAndUser_UserIdAndUser_UserIdContainingIgnoreCase(userId, keyword, pageable);
                break;
            case "제목+내용":
            case "전체":
            default:
                inquiryPage = inquiryRepository.searchPrivateInquiriesByAll(userId, keyword, pageable);
                break;
        }

        return inquiryPage.map(inquiry -> {
            Long replyCount = replyRepository.countByInquiry_InquiryId(inquiry.getInquiryId());
            return InquiryListResponseDto.fromEntity(inquiry, null, replyCount);
        });
    }
}