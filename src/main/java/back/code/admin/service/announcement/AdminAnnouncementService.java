package back.code.admin.service.announcement;


import back.code.admin.dto.announcement.AnnouncementCreateDTO;
import back.code.admin.dto.announcement.AnnouncementResponseDTO;
import back.code.admin.repository.AdminCommunityPostRepository;
import back.code.admin.repository.AdminInquiryRepository;
import back.code.community.entity.CommunityPostEntity;
import back.code.community.repository.CommunityBoardRepository;
import back.code.inquiry.entity.InquiryEntity;
import back.code.inquiry.entity.InquiryStatus;
import back.code.notice.service.NotificationService;
import back.code.user.entity.UserEntity;
import back.code.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminAnnouncementService {

    private final AdminInquiryRepository adminInquiryRepository;
    private final AdminCommunityPostRepository communityPostRepository;
    private final CommunityBoardRepository  communityBoardRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;



    /**
     * Inquiry 공개 데이터 + Community boardId=1 데이터 조회
     */
    @Transactional(readOnly = true)
    public List<AnnouncementResponseDTO> getAllAnnouncements() {

        List<AnnouncementResponseDTO> result = new ArrayList<>();

        // Inquiry 조회
        List<InquiryEntity> inquiries = adminInquiryRepository.findByIsPublic(true);
        inquiries.forEach(i -> result.add(
                AnnouncementResponseDTO.builder()
                        .type("INQUIRY")
                        .id(i.getInquiryId())
                        .title(i.getTitle())
                        .content(i.getContent())
                        .createAt(i.getCreateAt())
                        .build()
        ));

        // CommunityPost 조회(boardId = 1)
        List<CommunityPostEntity> posts = communityPostRepository.findByBoardBoardId(1);
        posts.forEach(p -> result.add(
                AnnouncementResponseDTO.builder()
                        .type("COMMUNITY_POST")
                        .id(Long.valueOf(p.getPostId()))
                        .title(p.getTitle())
                        .content(p.getContents())
                        .createAt(p.getCreateAt())
                        .build()
        ));

        // 최신순으로 정렬 (옵션: 필요 없다면 제거 가능)
        result.sort((a, b) -> b.getCreateAt().compareTo(a.getCreateAt()));

        // seq 번호 1부터 부여
        long seq = 1L;
        for (AnnouncementResponseDTO dto : result) {
            dto.setSeq(seq++);
        }

        log.info("[AdminAnnouncementService] 전체 공지 조회 후 {}건 순번 부여", result.size());

        return result;
    }

    @Transactional
    public void createAnnouncement(AnnouncementCreateDTO dto) {

        // (1) 관리자 계정 조회
        UserEntity adminUser = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        String title = dto.getTitle();
        String content = dto.getContent();

        Long announcementId; // 공지 ID 저장용

        switch (dto.getType()) {

            case "NOTICE": {
                CommunityPostEntity post = CommunityPostEntity.builder()
                        .title(title)
                        .contents(content)
                        .board(communityBoardRepository.getReferenceById(1))
                        .user(adminUser)
                        .likeCount(0)
                        .readCount(0)
                        .build();

                CommunityPostEntity saved = communityPostRepository.save(post);
                announcementId = Long.valueOf(saved.getPostId());
                break;
            }

            case "INQUIRY_RESPONSE": {
                InquiryEntity inquiry = InquiryEntity.builder()
                        .title(title)
                        .content(content)
                        .isPublic(true)
                        .status(InquiryStatus.answered)
                        .user(adminUser)
                        .build();

                InquiryEntity saved = adminInquiryRepository.save(inquiry);
                announcementId = saved.getInquiryId();
                break;
            }

            default:
                throw new IllegalArgumentException("지원하지 않는 공지 유형입니다.");
        }

        // (2) 🔔 전체 사용자에게 알림 발송
        notificationService.sendAnnouncementNotification(title, content, announcementId);

        log.info("[AdminAnnouncementService] 공지 생성 완료 및 전체 알림 발송 완료");
    }

    // 공지 삭제(커뮤니티)
    @Transactional
    public void deleteCommunity(Integer id) {
        communityPostRepository.deleteById(id);
    }

    // 공지 삭제(문의사항)
    @Transactional
    public void deleteInquiry(Long id) {
        adminInquiryRepository.deleteById(id);
    }

    // 공지 수정(커뮤니티)
    @Transactional
    public void updateCommunity(Integer id, AnnouncementCreateDTO dto) {
        CommunityPostEntity post = communityPostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 커뮤니티 공지를 찾을 수 없습니다."));

        post.setTitle(dto.getTitle());
        post.setContents(dto.getContent());
    }

    // 공지 수정(커뮤니티)
    @Transactional
    public void updateInquiry(Long id, AnnouncementCreateDTO dto) {
        InquiryEntity inquiry = adminInquiryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 문의 공지를 찾을 수 없습니다."));

        inquiry.setTitle(dto.getTitle());
        inquiry.setContent(dto.getContent());
    }

}
