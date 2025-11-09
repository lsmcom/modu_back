package back.code.inquiry.service;

import back.code.inquiry.dto.InquiryReplyRequestDto;
import back.code.inquiry.dto.InquiryReplyResponseDto;
import back.code.inquiry.entity.Inquiry;
import back.code.inquiry.entity.InquiryReply;
import back.code.inquiry.repository.InquiryReplyRepository;
import back.code.inquiry.repository.InquiryRepository;
import back.code.user.entity.UserEntity;
import back.code.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 4. 코드 내 수정된 부분을 명확히 표시: InquiryReply 서비스 (findById 사용 및 정렬 기준 createAt으로 변경)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryReplyService {

    private final InquiryReplyRepository replyRepository;
    private final InquiryRepository inquiryRepository;
    private final UserRepository userRepository;

    /**
     * 답변 등록 (InquiryDetail.jsx)
     */
    @Transactional
    public List<InquiryReplyResponseDto> createReply(Long inquiryId, InquiryReplyRequestDto requestDto) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("문의글을 찾을 수 없습니다: " + inquiryId));

        // 4. 코드 내 수정된 부분을 명확히 표시: findByUserId를 findById로 수정
        UserEntity admin = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("관리자 계정을 찾을 수 없습니다: " + requestDto.getUserId()));

        InquiryReply reply = InquiryReply.builder()
                .inquiry(inquiry)
                .admin(admin)
                .content(requestDto.getContent())
                .build();

        replyRepository.save(reply);

        return getRepliesByInquiryId(inquiryId);
    }

    /**
     * 특정 문의에 대한 답변 목록 조회
     */
    public List<InquiryReplyResponseDto> getRepliesByInquiryId(Long inquiryId) {
        // 4. 코드 내 수정된 부분을 명확히 표시: findByInquiry_InquiryIdOrderByRepliedAtAsc 대신 createAtAsc 사용
        return replyRepository.findByInquiry_InquiryIdOrderByCreateAtAsc(inquiryId)
                .stream()
                .map(InquiryReplyResponseDto::fromEntity)
                .collect(Collectors.toList());
    }
}