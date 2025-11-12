-- 업적 테이블
CREATE TABLE `milestone` (
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

CREATE TABLE `user_milestone` (
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