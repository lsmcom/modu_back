CREATE TABLE recent_search (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '최근 검색어 고유 ID',
    user_id VARCHAR(100) NOT NULL COMMENT '유저 ID (FK)',
    type VARCHAR(50) NOT NULL COMMENT '검색 대상 타입 (예: ACCOUNT, MEMO, TODO, PLAN)',
    keyword VARCHAR(255) NOT NULL COMMENT '검색어',
    CONSTRAINT fk_recent_search_user FOREIGN KEY (user_id)
        REFERENCES `user` (user_id)
        ON DELETE CASCADE
) COMMENT='사용자별 최근 검색어 테이블';


