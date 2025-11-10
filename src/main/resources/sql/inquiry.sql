DROP TABLE IF EXISTS inquiry_reply;
DROP TABLE IF EXISTS inquiry;



/* inquiry 테이블 수정 */
CREATE TABLE `inquiry` (
                           `inquiry_id`	BIGINT	NOT NULL AUTO_INCREMENT	COMMENT '문의사항 고유 ID (PK)',
                           `user_id`	varchar(100)	NOT NULL	COMMENT '문의 작성자 ID (FK)',
                           `title`	VARCHAR(255)	NOT NULL	COMMENT '문의 제목',
                           `content`	TEXT	NOT NULL	COMMENT '문의 내용',
                           `status`	ENUM('temp', 'submitted', 'answered')	NOT NULL	DEFAULT 'submitted'	COMMENT '문의 상태',
                           `is_public`	BOOLEAN	NOT NULL	DEFAULT FALSE	COMMENT 'FAQ 공개 여부',
                           `create_at`	DATETIME	NOT NULL	DEFAULT now()	COMMENT '작성일',
                           `update_at`	DATETIME	NULL	DEFAULT NULL	COMMENT '수정일',

                           PRIMARY KEY(`inquiry_id`),
                           CONSTRAINT fk_inquiry_user FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) COMMENT '문의 테이블';

/* inquiry_reply */
CREATE TABLE `inquiry_reply` (
                                 `reply_id`	BIGINT	NOT NULL AUTO_INCREMENT	COMMENT '답변 고유 ID (PK)',
                                 `inquiry_id`	BIGINT	NOT NULL	COMMENT '문의사항 ID (FK)',
                                 `admin_id`	VARCHAR(100)	NOT NULL	COMMENT '답변 관리자 ID (FK)',
                                 `content`	TEXT	NOT NULL	COMMENT '답변 내용',
                                 `create_at`	DATETIME	NOT NULL	DEFAULT now()	COMMENT '생성일 (BaseTimeEntity)',
                                 `update_at`	DATETIME	NULL	DEFAULT NULL	COMMENT '수정일 (BaseTimeEntity)',

                                 PRIMARY KEY(`reply_id`),
                                 CONSTRAINT fk_reply_inquiry FOREIGN KEY (`inquiry_id`) REFERENCES `inquiry` (`inquiry_id`),
                                 CONSTRAINT fk_reply_admin FOREIGN KEY (`admin_id`) REFERENCES `user` (`user_id`)
) COMMENT '문의 답변 테이블';
