-- 1. subtodolist 테이블 삭제 (가장 하위 테이블)
DROP TABLE IF EXISTS subtodolist;
-- 2. todolist 테이블 삭제
DROP TABLE IF EXISTS todolist;
-- 3. todofolder 테이블 삭제 (가장 상위 테이블)
DROP TABLE IF EXISTS todofolder;
-- 3. todo_completion_status 테이블 삭제
DROP TABLE IF EXISTS todo_completion_status;

/* 투두 폴더 */
CREATE TABLE todofolder (
                            folder_id INT PRIMARY KEY NOT NULL AUTO_INCREMENT COMMENT '폴더 고유 ID',
                            user_id VARCHAR(255) NOT NULL COMMENT '사용자 ID (FK)',
                            name VARCHAR(255) NOT NULL COMMENT '폴더 이름',

    -- 외래 키 정의: todofolder는 user 테이블의 user_id를 참조
                            CONSTRAINT fk_folder_user
                                FOREIGN KEY (user_id) REFERENCES user (user_id)
                                    ON DELETE CASCADE -- 사용자 탈퇴 시 해당 사용자의 모든 폴더 삭제
) COMMENT '할 일 폴더 테이블'
    AUTO_INCREMENT = 1000;

CREATE INDEX idx_folder_user_id ON todofolder (user_id);


/* 투두 */
CREATE TABLE todolist (
                          todo_id INT PRIMARY KEY NOT NULL AUTO_INCREMENT COMMENT '할 일 항목 고유 ID',
                          user_id VARCHAR(255) NOT NULL COMMENT '사용자 ID (FK)',
                          folder_id INT NOT NULL COMMENT '폴더 ID (FK)',
                          title VARCHAR(255) NOT NULL COMMENT '할 일 제목',
                          td_fixed TINYINT(1) NOT NULL DEFAULT 0 COMMENT '고정 핀 상태',
                          is_completed TINYINT(1) NOT NULL DEFAULT 0 COMMENT '완료 상태',
                          due_date DATETIME NULL COMMENT '마감일',
                          order_index INT NOT NULL COMMENT '정렬 순서 인덱스',
                          create_date DATETIME NOT NULL COMMENT '생성 일시',
                          repeat_days VARCHAR(255) NULL COMMENT '반복 요일 (콤마로 구분된 문자열: 0=월요일, 6=일요일)',
                          auto_migrate TINYINT(1) NULL COMMENT '미완료 시 익일 자동 이월 여부',

    -- 외래 키 정의 1: todolist는 user 테이블의 user_id를 참조
                          CONSTRAINT fk_todo_user
                              FOREIGN KEY (user_id) REFERENCES user (user_id)
                                  ON DELETE CASCADE, -- 사용자 탈퇴 시 해당 사용자의 모든 할 일 삭제

    -- 외래 키 정의 2: todolist는 todofolder 테이블의 folder_id를 참조
                          CONSTRAINT fk_todo_folder
                              FOREIGN KEY (folder_id) REFERENCES todofolder (folder_id)
                                  ON DELETE CASCADE -- 폴더 삭제 시 해당 폴더의 모든 할 일 삭제
) COMMENT '할 일 목록 테이블';

CREATE INDEX idx_user_folder_order ON todolist (user_id, folder_id, order_index);
CREATE INDEX idx_due_date ON todolist (due_date);

/* 1. sub_title 컬럼 정규화를 위한 subtodolist 테이블 생성 */
CREATE TABLE subtodolist (
                             id INT PRIMARY KEY NOT NULL AUTO_INCREMENT COMMENT '하위 할 일 항목 고유 ID',
                             todolist_id INT NOT NULL COMMENT '상위 할 일 항목 ID (FK)',
                             title VARCHAR(255) NOT NULL COMMENT '하위 할 일 내용',

    -- 외래 키 정의: subtodolist는 todolist 테이블의 todo_id를 참조
                             CONSTRAINT fk_subtodo_todo
                                 FOREIGN KEY (todolist_id) REFERENCES todolist (todo_id)
                                     ON DELETE CASCADE -- 상위 할 일 삭제 시 하위 할 일 삭제
) COMMENT '하위 할 일 목록 테이블';

CREATE INDEX idx_subtodo_todolist_id ON subtodolist (todolist_id);

CREATE TABLE todo_completion_status (
                                          `user_id` VARCHAR(100) NOT NULL,
                                          `completed_count` INT NOT NULL DEFAULT 0,
                                          `last_updated` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                          PRIMARY KEY (`user_id`)
) COMMENT '투두 완료 카운트';


-- 1. todofolder 테이블 데이터 삽입
INSERT INTO todofolder (folder_id, user_id, name) VALUES
                                                      (0, 'user01', '전체'),
                                                      (1, 'user01', '기본 폴더'),
                                                      (2, 'user01', '개인'),
                                                      (3, 'user01', '업무'),
                                                      (999, 'user01', 'NotTodo')
ON DUPLICATE KEY UPDATE name = VALUES(name); -- 중복 삽입 방지 로직 추가 (선택 사항)


-- ******************************************************
-- 2. todolist 테이블 데이터 삽입 (Todo 항목)
-- ******************************************************

INSERT INTO todolist (user_id, folder_id, title, td_fixed, is_completed, due_date, order_index, create_date, repeat_days, auto_migrate)
VALUES
    -- Todo 1: 고정된 미완료 할 일 (기본 폴더)
    ('user01', 1, '긴급 보고서 작성', 1, 0, NOW() + INTERVAL 1 DAY, 0, NOW(), NULL, 1),

    -- Todo 2: 완료된 할 일 (업무 폴더)
    ('user01', 3, '팀 회의 자료 검토', 0, 1, NOW() - INTERVAL 1 HOUR, 1, NOW() - INTERVAL 1 DAY, NULL, 0),

    -- Todo 3: 반복 설정된 할 일 (개인 폴더, 월/수/금 반복, 월요일=0, 수요일=2, 금요일=4)
    ('user01', 2, '헬스장 방문 (반복)', 0, 0, NOW() + INTERVAL 1 DAY, 2, NOW(), '0,2,4', 1),

    -- Todo 4: 마감일 없는 할 일 (기본 폴더)
    ('user01', 1, '주간 목표 설정 검토', 0, 0, NULL, 3, NOW(), NULL, 0),

    -- Todo 5: 미완료 할 일 (NotTodo 폴더)
    ('user01', 999, '핸드폰 게임 3시간 하기', 0, 0, NOW() + INTERVAL 3 DAY, 4, NOW(), NULL, 0);


-- ******************************************************
-- 3. subtodolist 테이블 데이터 삽입 (하위 Todo 항목)
-- (Todo 1의 todo_id를 1로, Todo 3의 todo_id를 3으로 가정)
-- 참고: Auto_increment가 1부터 시작한다고 가정하고 FK를 직접 명시합니다.
-- 실제 환경에서는 last_insert_id() 등을 사용해야 합니다.
-- ******************************************************

INSERT INTO subtodolist (todolist_id, title)
VALUES
    -- Todo 1의 하위 항목
    (1, '데이터 분석 섹션 완료'),
    (1, '차트 디자인 검토'),

    -- Todo 3의 하위 항목
    (3, '유산소 30분'),
    (3, '근력 운동 40분');


-- ******************************************************
-- 4. todo_completion_status 테이블 데이터 삽입
-- (Milestone 로직 테스트를 위해 Todo 2 완료 기록 1개를 반영)
-- ******************************************************

INSERT INTO todo_completion_status (user_id, completed_count, last_updated)
VALUES
    ('user01', 1, NOW() - INTERVAL 1 DAY)
ON DUPLICATE KEY UPDATE completed_count = VALUES(completed_count);