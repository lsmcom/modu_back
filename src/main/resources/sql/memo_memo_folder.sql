-- 기존 테이블 제거
DROP TABLE IF EXISTS memo;
DROP TABLE IF EXISTS memo_folder;

--  메모 폴더 테이블 (user_id 추가)
CREATE TABLE memo_folder (
                             folder_id    INT AUTO_INCREMENT PRIMARY KEY COMMENT '폴더 고유 ID (자동 증가)',
                             user_id      VARCHAR(100) NOT NULL COMMENT '소유자 ID (FK)',  --  추가
                             folder_name  VARCHAR(50) NOT NULL COMMENT '폴더명',
                             CONSTRAINT fk_memo_folder_user
                                 FOREIGN KEY (user_id)
                                     REFERENCES `user` (user_id)
                                     ON DELETE CASCADE
) COMMENT='메모 폴더 테이블';

--  메모 테이블
CREATE TABLE memo (
                      memo_id        INT AUTO_INCREMENT PRIMARY KEY COMMENT '메모 고유 ID (자동 증가)',
                      user_id        VARCHAR(100) NOT NULL COMMENT '작성자 ID (FK)',
                      folder_id      INT NOT NULL COMMENT '폴더 ID (FK)',
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
                              ON DELETE CASCADE
) COMMENT='메모 테이블';

--  테스트 데이터
INSERT INTO memo_folder (user_id, folder_name)
VALUES
    ('user01', '폴더1'),
    ('user01', '폴더2'),
    ('user01', '폴더3'),
    ('user01', '폴더4');

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

--  검증용
SELECT * FROM memo_folder;
SELECT * FROM memo;
select * from file;


-- 여기부터 추가
ALTER TABLE memo_folder
ADD COLUMN folder_type ENUM('ALL', 'DEFAULT', 'NORMAL') NOT NULL DEFAULT 'NORMAL'
COMMENT '폴더 타입 (전체, 기본, 일반)';