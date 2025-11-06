package back.code.community.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommunityPostFileId implements Serializable {
    private Integer post;
    private String file;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommunityPostFileId)) return false;
        CommunityPostFileId that = (CommunityPostFileId) o;
        return Objects.equals(post, that.post) && Objects.equals(file, that.file);
    }

    @Override
    public int hashCode() {
        return Objects.hash(post, file);
    }
}
