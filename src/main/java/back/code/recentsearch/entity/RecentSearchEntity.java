package back.code.recentsearch.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "recent_search",
        uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "type", "keyword"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecentSearchEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String userId;

    @Column(nullable = false, length = 50)
    private String type; // 예: MEMO, TODO, ACCOUNT, PLAN

    @Column(nullable = false, length = 255)
    private String keyword;

}
