package back.code.community.dto;

import back.code.community.entity.enum_.ImageSizeType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommunityPostSettingDTO {
    private String isPublic;
    private String isSearch;
    private String isComment;
    private String isInShare;
    private String isCopy;
    private String isOutShare;
    private ImageSizeType imageSizeType;
}
