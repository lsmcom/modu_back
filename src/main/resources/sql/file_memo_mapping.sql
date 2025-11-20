DROP TABLE IF EXISTS file_memo_mapping;

/* 매핑 테이블 생성 */
CREATE TABLE file_memo_mapping (
    memo_id     INT NOT NULL COMMENT '메모 ID (FK)',
    file_id     VARCHAR(255) NOT NULL COMMENT '파일 ID (FK)',
    
    PRIMARY KEY (memo_id, file_id),  -- 복합 기본키

    CONSTRAINT fk_memo_file_memo
        FOREIGN KEY (memo_id)
        REFERENCES memo (memo_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_memo_file_file
        FOREIGN KEY (file_id)
        REFERENCES file (file_id)
        ON DELETE CASCADE
) COMMENT='메모-파일 매핑 테이블';

DELETE FROM category
WHERE user_id = '123123123';

DELETE FROM user
WHERE user_id = '123123123';
