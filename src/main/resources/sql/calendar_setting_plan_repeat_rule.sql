-- calendar_setting (사용자별 캘린더 환경 설정)
CREATE TABLE calendar_setting (
    setting_id       BIGINT NOT NULL AUTO_INCREMENT COMMENT '설정 고유 ID (자동 증가)',
    user_id          VARCHAR(100) NOT NULL COMMENT '사용자 ID (FK → user.user_id)',
    enable_notification CHAR(1) DEFAULT 'N' COMMENT '알림 활성화 여부 (Y/N)',
    share_plan_color VARCHAR(10) DEFAULT '#A9EDED' COMMENT '공유 일정 색상',
    default_view     VARCHAR(20) DEFAULT 'dayGridMonth' COMMENT '기본 캘린더 뷰 (month/week/day)',
    time_zone        VARCHAR(50) DEFAULT 'Asia/Seoul' COMMENT '기본 시간대',
    PRIMARY KEY (setting_id),
    UNIQUE KEY uq_calendar_setting_user (user_id),
    CONSTRAINT fk_calendar_setting_user FOREIGN KEY (user_id)
        REFERENCES user(user_id)
        ON DELETE CASCADE
) COMMENT='사용자별 캘린더 환경 설정 테이블';

INSERT INTO calendar_setting(user_id)
VALUES ('user01');


-- 반복 규칙 테이블 생성
CREATE TABLE plan_repeat_rule (
    rule_id     BIGINT NOT NULL AUTO_INCREMENT COMMENT '반복 규칙 고유 ID (자동 증가)',
    plan_id     BIGINT NOT NULL COMMENT '원본 일정 ID (FK → plan.plan_id)',
    repeat_type VARCHAR(10) NOT NULL COMMENT '반복 유형 (매일, 매주, 매월)',
    PRIMARY KEY (rule_id),
    CONSTRAINT fk_plan_repeat_rule_plan FOREIGN KEY (plan_id)
        REFERENCES plan(plan_id)
        ON DELETE CASCADE
) COMMENT='일정 반복 규칙 테이블 (매일/매주/매월)';

ALTER TABLE calendar_setting
    ADD COLUMN show_repeat_plan CHAR(1) DEFAULT 'Y' COMMENT '반복 일정 표시 여부 (Y/N)';

ALTER TABLE calendar_setting
    DROP COLUMN enable_notification;