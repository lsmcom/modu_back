package back.code.community.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "community_board")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CommunityBoardEntity {

    @Id
    private Integer boardId; // 게시판 번호
    private String boardName; // 게시판 명
}
