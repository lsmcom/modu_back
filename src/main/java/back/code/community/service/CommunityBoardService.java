package back.code.community.service;

import back.code.community.entity.CommunityBoardEntity;
import back.code.community.repository.CommunityBoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommunityBoardService {

    private final CommunityBoardRepository communityBoardRepository;

    /** 게시판 목록 조회 */
    public List<CommunityBoardEntity> getBoardList() {
        return communityBoardRepository.findAll();
    }
}
