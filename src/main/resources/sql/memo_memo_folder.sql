DROP TABLE IF EXISTS memo;
DROP TABLE IF EXISTS memo_folder;

CREATE TABLE memo_folder (
                             folder_id    INT AUTO_INCREMENT PRIMARY KEY COMMENT '폴더 고유 ID (자동 증가)',
                             folder_name  VARCHAR(50) NOT NULL COMMENT '폴더명'
) COMMENT='메모 폴더 테이블';

CREATE TABLE memo (
                      memo_id        INT AUTO_INCREMENT PRIMARY KEY COMMENT '메모 고유 ID (자동 증가)',
                      user_id        VARCHAR(100) NOT NULL COMMENT '작성자 ID (FK)',
                      folder_id      INT NOT NULL COMMENT '폴더 ID (FK)', -- ✅ NOT NULL로 변경
                      memo_title     VARCHAR(50) COMMENT '메모 제목',
                      memo_contents  TEXT COMMENT '메모 내용',
                      is_fixed       CHAR(1) DEFAULT 'N' COMMENT '상단 고정 여부 (Y/N)',
                      create_date    DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
                      update_date    DATETIME DEFAULT CURRENT_TIMESTAMP
                          ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일 자동 갱신',
                      CONSTRAINT fk_memo_user
                          FOREIGN KEY (user_id)
                              REFERENCES user (user_id)
                              ON DELETE CASCADE,
                      CONSTRAINT fk_memo_folder
                          FOREIGN KEY (folder_id)
                              REFERENCES memo_folder (folder_id)
                              ON DELETE CASCADE  -- 폴더 삭제 시 메모도 자동 삭제
) COMMENT='메모 테이블';


INSERT INTO memo_folder (folder_name)
VALUES 
    ('폴더1'),
    ('폴더2'),
    ('폴더3'),
    ('폴더4');

INSERT INTO memo (user_id, folder_id, memo_title, memo_contents, is_fixed)
VALUES
-- 폴더1
('user01', 1, '폴더1메모예시1', '폴더1의 첫 번째 메모 내용입니다.', 'N'),
('user01', 1, '폴더1메모예시2', '폴더1의 두 번째 메모 내용입니다.', 'N'),
('user01', 1, '폴더1메모예시3', '폴더1의 세 번째 메모 내용입니다.', 'N'),
('user01', 1, '폴더1메모예시4', '폴더1의 네 번째 메모 내용입니다.', 'Y'),

-- 폴더2
('user01', 2, '폴더2메모예시1', '폴더2의 첫 번째 메모 내용입니다.', 'N'),
('user01', 2, '폴더2메모예시2', '폴더2의 두 번째 메모 내용입니다.', 'N'),
('user01', 2, '폴더2메모예시3', '폴더2의 세 번째 메모 내용입니다.', 'N'),
('user01', 2, '폴더2메모예시4', '폴더2의 네 번째 메모 내용입니다.', 'Y'),

-- 폴더3
('user01', 3, '폴더3메모예시1', '폴더3의 첫 번째 메모 내용입니다.', 'N'),
('user01', 3, '폴더3메모예시2', '폴더3의 두 번째 메모 내용입니다.', 'N'),
('user01', 3, '폴더3메모예시3', '폴더3의 세 번째 메모 내용입니다.', 'N'),
('user01', 3, '폴더3메모예시4', '폴더3의 네 번째 메모 내용입니다.', 'Y'),

-- 폴더4
('user01', 4, '폴더4메모예시1', '폴더4의 첫 번째 메모 내용입니다.', 'N'),
('user01', 4, '폴더4메모예시2', '폴더4의 두 번째 메모 내용입니다.', 'N'),
('user01', 4, '폴더4메모예시3', '폴더4의 세 번째 메모 내용입니다.', 'N'),
('user01', 4, '폴더4메모예시4', '폴더4의 네 번째 메모 내용입니다.', 'Y');


select * from memo;