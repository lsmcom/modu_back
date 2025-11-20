package back.code.community.controller;

import back.code.common.dto.ApiResponse;
import back.code.community.dto.CommunitySearchResponse;
import back.code.community.service.CommunitySearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/community/search")
public class CommunitySearchController {

    private final CommunitySearchService searchService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CommunitySearchResponse>>> searchPosts(@RequestParam("keyword") String keyword,
            @RequestParam(name="range", defaultValue = "전체") String range, @RequestParam(name="boardId", required = false) Integer boardId,
            @RequestParam("userId") String userId) {
        List<CommunitySearchResponse> results = searchService.searchPosts(keyword, range, boardId);
        return ResponseEntity.ok(ApiResponse.ok(results));
    }
}
