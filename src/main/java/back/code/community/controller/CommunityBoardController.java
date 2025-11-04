package back.code.community.controller;

import back.code.common.dto.ApiResponse;
import back.code.community.entity.CommunityBoardEntity;
import back.code.community.service.CommunityBoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/community/boards")
public class CommunityBoardController {

    private final CommunityBoardService communityBoardService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CommunityBoardEntity>>> getBoardList() {
        List<CommunityBoardEntity> boardList = communityBoardService.getBoardList();
        return ResponseEntity.ok(ApiResponse.ok(boardList));
    }
}
