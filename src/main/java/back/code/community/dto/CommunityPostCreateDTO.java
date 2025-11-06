package back.code.community.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CommunityPostCreateDTO {
    private Integer postId;
    private String userId;
    private Integer boardId; // 게시판 번호 (임시저장은 null 가능)
    private String title;
    private String contents;
    private Character isTemporary; // 'Y' or 'N'
    private CommunityPostSettingDTO setting;

    // 첨부파일 갱신 정책
    private Boolean replaceAll; // true면 기존 매핑 전부 교체
    private List<String> keepFileIds; // 유지할(기존) 파일ID 목록
}
