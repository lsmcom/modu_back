package back.code.community.service;

import back.code.community.dto.CommunityPostDTO;
import back.code.community.repository.CommunityFixedNoticeRepository;
import back.code.community.repository.CommunityPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommunityPostService {

    private final CommunityPostRepository communityPostRepository;
    private final CommunityFixedNoticeRepository communityFixedNoticeRepository;

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
}
