package back.code.admin.service.announcement;


import back.code.admin.dto.announcement.AnnouncementResponseDTO;
import back.code.admin.repository.AdminCommunityPostRepository;
import back.code.admin.repository.AdminInquiryRepository;
import back.code.community.entity.CommunityPostEntity;
import back.code.inquiry.entity.InquiryEntity;
import back.code.inquiry.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminAnnouncementService {

    private final AdminInquiryRepository adminInquiryRepository;
    private final AdminCommunityPostRepository communityPostRepository;



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

}
