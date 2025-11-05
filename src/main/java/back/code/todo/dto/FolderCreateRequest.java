package back.code.todo.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FolderCreateRequest {

    // TodoContext.js의 handleSubmitFolder 함수에서 입력받는 name에 매핑
    private String name;    // 폴더 이름 (필수)

    // 폴더를 특정 사용자에게 귀속시키기 위한 user_id는
    // 서버의 인증/보안 컨텍스트에서 추출하므로 DTO에는 포함하지 않습니다.
}
