-- 기존 테이블 전부 제거 (외래키 제약 포함)
DROP TABLE IF EXISTS plan_share;
DROP TABLE IF EXISTS plan;
DROP TABLE IF EXISTS calendar_folder;

-- calendar_folder (PK: BIGINT AUTO_INCREMENT)
CREATE TABLE calendar_folder (
                                 folder_id    BIGINT NOT NULL AUTO_INCREMENT COMMENT '폴더 고유 ID (자동 증가)',
                                 user_id      VARCHAR(100) NOT NULL COMMENT '폴더 소유자 ID (FK)',
                                 folder_name  VARCHAR(50) NOT NULL COMMENT '폴더명',
                                 create_date  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
                                 update_date  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
                                 PRIMARY KEY (folder_id),
                                 CONSTRAINT fk_calendar_folder_user FOREIGN KEY (user_id)
                                     REFERENCES user(user_id)
                                     ON DELETE CASCADE
) COMMENT='캘린더용 폴더 테이블 (폴더 단위로 일정 관리)';

-- plan (FK → calendar_folder.folder_id)
CREATE TABLE plan (
                      plan_id       BIGINT NOT NULL AUTO_INCREMENT COMMENT '일정 고유 ID (자동 증가)',
                      user_id       VARCHAR(100) NOT NULL COMMENT '작성자 ID (FK)',
                      folder_id     BIGINT NOT NULL COMMENT '캘린더 폴더 ID (FK)',
                      plan_title    VARCHAR(100) NOT NULL COMMENT '일정 제목',
                      plan_content  TEXT COMMENT '일정 내용',
                      start_time    DATETIME NOT NULL COMMENT '시작 일시',
                      end_time      DATETIME NOT NULL COMMENT '종료 일시',
                      repeat_type   VARCHAR(10) DEFAULT '없음' COMMENT '반복 유형 (없음, 매일, 매주, 매월 등)',
                      color         VARCHAR(10) DEFAULT '#aaeded' COMMENT '일정 색상',
                      reminder      VARCHAR(10) DEFAULT '없음' COMMENT '알림 설정',
                      create_date   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
                      update_date   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
                      PRIMARY KEY (plan_id),
                      CONSTRAINT fk_plan_user FOREIGN KEY (user_id)
                          REFERENCES user(user_id)
                          ON DELETE CASCADE,
                      CONSTRAINT fk_plan_calendar_folder FOREIGN KEY (folder_id)
                          REFERENCES calendar_folder(folder_id)
                          ON DELETE CASCADE
) COMMENT='일정(Plan) 테이블 - 반복, 색상, 알림, 공유 인원 포함';

-- plan_share (plan과 user 연결)
CREATE TABLE plan_share (
                            plan_id         BIGINT NOT NULL COMMENT '공유 일정 ID (FK)',
                            shared_user_id  VARCHAR(100) NOT NULL COMMENT '공유 대상 사용자 ID (FK)',
                            PRIMARY KEY (plan_id, shared_user_id),
                            CONSTRAINT fk_plan_share_plan FOREIGN KEY (plan_id)
                                REFERENCES plan(plan_id)
                                ON DELETE CASCADE,
                            CONSTRAINT fk_plan_share_user FOREIGN KEY (shared_user_id)
                                REFERENCES user(user_id)
                                ON DELETE CASCADE
) COMMENT='일정 공유 관계 테이블 (N:N 매핑, 공유 사용자 관리)';

-- 샘플 데이터 삽입 (이제 folder_id는 숫자 자동 증가)
INSERT INTO calendar_folder (user_id, folder_name)
VALUES
    ('user01', '폴더1'),
    ('user01', '폴더2'),
    ('user01', '폴더3'),
    ('user01', '폴더4');

-- 폴더별 plan 데이터 삽입
INSERT INTO plan (user_id, folder_id, plan_title, plan_content, start_time, end_time, repeat_type, color, reminder)
VALUES
-- 폴더1 일정
('user01', 1, '폴더1일정1', '회의 준비', '2025-11-05 09:00:00', '2025-11-05 10:00:00', '없음', '#FFB6C1', '없음'),
('user01', 1, '폴더1일정2', '자료 검토', '2025-11-06 14:00:00', '2025-11-06 15:30:00', '없음', '#87CEFA', '없음'),

-- 폴더2 일정
('user01', 2, '폴더2일정1', '팀 회의', '2025-11-07 10:00:00', '2025-11-07 11:00:00', '없음', '#98FB98', '없음'),
('user01', 2, '폴더2일정2', '디자인 검수', '2025-11-08 13:00:00', '2025-11-08 14:00:00', '없음', '#FFD700', '없음'),

-- 폴더3 일정
('user01', 3, '폴더3일정1', '기획 회의', '2025-11-09 09:00:00', '2025-11-09 10:30:00', '없음', '#DA70D6', '없음'),
('user01', 3, '폴더3일정2', '문서 작성', '2025-11-10 15:00:00', '2025-11-10 17:00:00', '없음', '#FFA07A', '없음'),

-- 폴더4 일정
('user01', 4, '폴더4일정1', '클라이언트 미팅', '2025-11-11 11:00:00', '2025-11-11 12:00:00', '없음', '#20B2AA', '없음'),
('user01', 4, '폴더4일정2', '개발 리뷰', '2025-11-12 16:00:00', '2025-11-12 18:00:00', '없음', '#FF6347', '없음');

ALTER TABLE calendar_folder
    ADD COLUMN folder_type VARCHAR(20) DEFAULT 'PERSONAL' COMMENT '폴더 유형 (PERSONAL, SHARED)';

INSERT INTO calendar_folder (user_id, folder_name, folder_type)
VALUES
    ('user01', '공유 폴더', 'SHARED');

INSERT INTO calendar_folder (user_id, folder_name, folder_type)
VALUES
    ('admin01', '공유 폴더', 'SHARED');

INSERT INTO calendar_folder (user_id, folder_name, folder_type)
SELECT u.user_id, '공유 폴더', 'SHARED'
FROM user u
WHERE NOT EXISTS (
    SELECT 1
    FROM calendar_folder f
    WHERE f.user_id = u.user_id
      AND f.folder_type = 'SHARED'
);