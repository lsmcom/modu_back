DROP TABLE IF EXISTS milestone;
DROP TABLE IF EXISTS user_milestone;
-- 업적 테이블
CREATE TABLE milestone (
                             `milestone_id`	BIGINT	NOT NULL	AUTO_INCREMENT	COMMENT '마일스톤 고유 ID (PK)',
                             `name`	VARCHAR(100)	NOT NULL	COMMENT '마일스톤명',
                             `description`	TEXT	NULL	COMMENT '마일스톤 설명',
                             `category`	VARCHAR(50)	NOT NULL	COMMENT '마일스톤 카테고리 (예: COUNT_TODO_WRITE, COUNT_TODO_COMPLETE)',
                             `target_type`	VARCHAR(50)	NOT NULL	COMMENT '마일스톤 목표 타입 (예: TODO_WRITE, TODO_COMPLETE)',
                             `target_value`	INT	NOT NULL	COMMENT '마일스톤 달성 목표 값 (예: 10, 50, 100)',
                             `reward`	VARCHAR(255)	NULL	COMMENT '마일스톤 보상 (예: 뱃지, 포인트)',
                             `is_hidden`	BOOLEAN	NOT NULL	DEFAULT FALSE	COMMENT '숨겨진 마일스톤 여부',
                             `create_date`	DATETIME	NOT NULL	DEFAULT CURRENT_TIMESTAMP	COMMENT '생성일',

                             PRIMARY KEY (`milestone_id`),
    -- 같은 목표 타입과 목표 값을 가지는 중복 마일스톤 방지
                             UNIQUE KEY `uk_target` (`target_type`, `target_value`)
) COMMENT '마일스톤 목록 테이블';

-- 유저 달성 업적 테이블

CREATE TABLE user_milestone (
                                  `user_id`	VARCHAR(100)	NOT NULL	COMMENT '회원 아이디 (FK)',
                                  `milestone_id`	BIGINT	NOT NULL	COMMENT '마일스톤 고유 ID (FK)',
                                  `achieved_at`	DATETIME	NOT NULL	DEFAULT CURRENT_TIMESTAMP	COMMENT '마일스톤 달성 시간',

                                  PRIMARY KEY (`user_id`, `milestone_id`),
    -- user 테이블과의 외래 키 연결
                                  CONSTRAINT `fk_user_milestone_user`
                                      FOREIGN KEY (`user_id`)
                                          REFERENCES `user` (`user_id`)
                                          ON DELETE CASCADE,
    -- milestone 테이블과의 외래 키 연결
                                  CONSTRAINT `fk_user_milestone_milestone`
                                      FOREIGN KEY (`milestone_id`)
                                          REFERENCES `milestone` (`milestone_id`)
                                          ON DELETE CASCADE
) COMMENT '사용자 마일스톤 달성 기록 테이블';

INSERT INTO `milestone`
(`name`, `description`, `category`, `target_type`, `target_value`, `reward`, `is_hidden`, `create_date`)
VALUES
    ('첫 걸음마', 'Todo 10개를 완료하여 첫 걸음을 내디뎠습니다.', 'TODO_COUNT', 'TODO_COMPLETE', 10, '브론즈 뱃지', FALSE, NOW()),
    ('작은 성공', 'Todo 20개 완료! 꾸준함이 비결입니다.', 'TODO_COUNT', 'TODO_COMPLETE', 20, '실버 뱃지', FALSE, NOW()),
    ('성장의 증거', 'Todo 30개 완료! 당신은 목표 달성가입니다.', 'TODO_COUNT', 'TODO_COMPLETE', 30, '골드 뱃지', FALSE, NOW()),
    ('최고의 달성가', 'Todo 40개 완료! 이제 목표를 더 높여보세요.', 'TODO_COUNT', 'TODO_COMPLETE', 40, '플래티넘 뱃지', FALSE, NOW())
ON DUPLICATE KEY UPDATE
                     -- 'uk_target' (`target_type`, `target_value`) 중복 시 업데이트 방지 (필요에 따라 name, description 등 업데이트 가능)
                     `name` = VALUES(`name`),
                     `description` = VALUES(`description`);