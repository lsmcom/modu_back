package back.code.community.dto;

import back.code.community.entity.enum_.ImageSizeType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommunityPostSettingDTO {
    private Character isPublic;
    private Character isSearch;
    private Character isComment;
    private Character isInShare;
    private Character isCopy;
    private Character isOutShare;
    private ImageSizeType imageSizeType;
}
