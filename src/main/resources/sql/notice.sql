DROP TABLE IF EXISTS notification;
CREATE TABLE notification (
                                `notification_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '알림 고유 ID (PK)',
                                `user_id` VARCHAR(100) NOT NULL,
                                `milestone_id` BIGINT NULL COMMENT '관련 업적 ID (Milestone 테이블 참조)',
                                `inquiry_id` BIGINT NULL COMMENT '관련 문의사항 ID (문의 답변 시)',
                                `type` ENUM('공지', '업적', '일정 공유', '문의 답변') NOT NULL COMMENT '알림 유형',
                                `title` VARCHAR(255) NOT NULL COMMENT '알림 제목',
                                `is_read` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '읽음 여부',
                                `create_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
                                `content` TEXT NULL COMMENT '알림 내용',

                                PRIMARY KEY (`notification_id`),
                                INDEX `idx_user_created` (`user_id`, `create_date` DESC),

    -- user 테이블과의 외래 키 연결
                                CONSTRAINT `fk_notification_user`
                                    FOREIGN KEY (`user_id`)
                                        REFERENCES `user` (`user_id`)
                                        ON DELETE CASCADE,

    -- milestone 테이블과의 외래 키 연결 (옵션. 알림이 업적과 연결될 때 유효성 검사)
                                CONSTRAINT `fk_notification_milestone`
                                    FOREIGN KEY (`milestone_id`)
                                        REFERENCES `milestone` (`milestone_id`)
                                        ON DELETE SET NULL
) COMMENT '알림 테이블';

ALTER TABLE notification
    ADD COLUMN sender_id VARCHAR(100) NULL COMMENT '알림 보낸 사용자 ID';

ALTER TABLE notification
    ADD COLUMN plan_id BIGINT NULL AFTER sender_id;

ALTER TABLE notification
    MODIFY COLUMN type ENUM(
        'community_announcement',
        'inquiry_announcement',
        'milestone',
        'planshare',
        'planshare_request',
        'planshare_accept',
        'planshare_reject',
        'planshare_update',
        'inquiryAnswer',
        'comment'
        ) NOT NULL COMMENT '알림 유형';

ALTER TABLE notification
    ADD COLUMN reference_id BIGINT NULL COMMENT '관련 게시글 ID -> type과 합쳐서 특정 가능';

-- milestone_id FK 제약 조건 삭제
ALTER TABLE notification
    DROP FOREIGN KEY fk_notification_milestone;
-- milestone_id 컬럼 삭제
ALTER TABLE notification
    DROP COLUMN milestone_id;
-- inquiry_id 컬럼 삭제
ALTER TABLE notification
    DROP COLUMN inquiry_id;


-- 11.20 수정
ALTER TABLE notification
MODIFY COLUMN type ENUM(
  'community_announcement','inquiry_announcement','milestone','planshare',
  'planshare_request','planshare_accept','planshare_reject','planshare_update',
  'inquiryAnswer','comment','account_goals','account_budget'
) NOT NULL COMMENT '알림 유형';