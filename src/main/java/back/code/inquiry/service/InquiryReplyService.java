package back.code.inquiry.service;

import back.code.common.utils.SecurityUtils;
import back.code.inquiry.dto.InquiryReplyRequest;
import back.code.inquiry.dto.InquiryReplyResponse;
import back.code.inquiry.entity.InquiryEntity;
import back.code.inquiry.entity.InquiryReplyEntity;
import back.code.inquiry.entity.InquiryStatus;
import back.code.inquiry.repository.InquiryReplyRepository;
import back.code.inquiry.repository.InquiryRepository;
import back.code.milestone.entity.Milestone;
import back.code.notice.entity.Notification;
import back.code.notice.repository.NotificationRepository;
import back.code.user.entity.UserEntity;
import back.code.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InquiryReplyService {

    private final InquiryRepository inquiryRepository;
    private final InquiryReplyRepository replyRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    /** 관리자 답변 등록 */
    @Transactional
    public InquiryReplyResponse createReply(InquiryReplyRequest req) {

        String userId = SecurityUtils.getCurrentUserId();
        String roleId = SecurityUtils.getCurrentUserRole();

        if (!"ROLE_ADMIN".equals(roleId)) {
            throw new RuntimeException("관리자만 답변을 작성할 수 있습니다.");
        }

        InquiryEntity inquiry = inquiryRepository.findById(req.getInquiryId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 문의입니다."));

        UserEntity admin = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("관리자 계정이 존재하지 않습니다."));

        inquiry.setStatus(InquiryStatus.answered);

        InquiryReplyEntity reply = replyRepository.save(
                InquiryReplyEntity.builder()
                        .inquiry(inquiry)
                        .admin(admin)
                        .content(req.getContent())
                        .build()
        );

        inquiryNotification(inquiry.getUser().getUserId(), inquiry);

        return InquiryReplyResponse.builder()
                .replyId(reply.getReplyId())
                .content(reply.getContent())
                .adminNick(admin.getUserNick())
                .createAt(reply.getCreateAt())
                .build();
    }

    /** 알림 테이블에 추가 */
    private void inquiryNotification(String userId, InquiryEntity inquiry) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(Notification.NotificationType.inquiryAnswer);

        notification.setReferenceId(inquiry.getInquiryId());
        notification.setTitle("[문의사항] 관리자님이 댓글을 등록하셨습니다.");
        notification.setContent(inquiry.getTitle());
        notification.setIsRead(false);

        notificationRepository.save(notification);
    }


    /** 답변 목록 조회 */
    @Transactional
    public List<InquiryReplyResponse> getReplies(Long inquiryId) {
        List<InquiryReplyEntity> replies = replyRepository.findByInquiry_InquiryIdOrderByCreateAtAsc(inquiryId);

        return replies.stream()
                .map(r -> InquiryReplyResponse.builder()
                        .replyId(r.getReplyId())
                        .content(r.getContent())
                        .adminNick(r.getAdmin().getUserNick())
                        .createAt(r.getCreateAt())
                        .build()
                ).toList();
    }

}
