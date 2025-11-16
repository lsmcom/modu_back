DROP TABLE IF EXISTS subtodolist;
DROP TABLE IF EXISTS todolist;
DROP TABLE IF EXISTS todofolder;
DROP TABLE IF EXISTS todo_completion_status;

/* 투두 폴더 */
CREATE TABLE todofolder (
  folder_id INT NOT NULL COMMENT '사용자별 폴더 고유 ID',
  user_id VARCHAR(100) NOT NULL COMMENT '사용자 ID (FK) - user 테이블과 타입 일치',
  name VARCHAR(255) NOT NULL COMMENT '폴더 이름',
  is_default TINYINT(1) NOT NULL DEFAULT 0 COMMENT '기본 폴더 여부 (1: 기본 폴더, 0: 사용자 정의 폴더)',
  
  PRIMARY KEY (user_id, folder_id), /* user_id와 folder_id를 복합 기본 키로 설정해 user_id를 통한 검색 효율 및 유일성 보장 */
  UNIQUE KEY uk_folder_user_name (user_id, name), /* user_id별 폴더 이름 중복 방지 */
  
  CONSTRAINT fk_folder_user   /* 외래 키 정의: todofolder는 user 테이블의 user_id를 참조 */
    FOREIGN KEY (user_id) REFERENCES user (user_id)
      ON DELETE CASCADE -- 사용자 탈퇴 시 해당 사용자의 모든 폴더 삭제
) COMMENT '할 일 폴더 테이블';

/* 투두 */
CREATE TABLE todolist (
                          todo_id INT PRIMARY KEY NOT NULL AUTO_INCREMENT COMMENT '할 일 항목 고유 ID',
                          user_id VARCHAR(100) NOT NULL COMMENT '사용자 ID (FK)',
                          folder_id INT NOT NULL COMMENT '폴더 ID (FK)',
                          title VARCHAR(255) NOT NULL COMMENT '할 일 제목',
                          td_fixed TINYINT(1) NOT NULL DEFAULT 0 COMMENT '고정 핀 상태',
                          is_completed TINYINT(1) NOT NULL DEFAULT 0 COMMENT '완료 상태',
                          due_date DATETIME NULL COMMENT '마감일',
                          order_index INT NOT NULL COMMENT '정렬 순서 인덱스',
                          create_date DATETIME NOT NULL COMMENT '생성 일시',
                          repeat_days VARCHAR(255) NULL COMMENT '반복 요일 (콤마로 구분된 문자열: 0=월요일, 6=일요일)',
                          auto_migrate TINYINT(1) NULL COMMENT '미완료 시 익일 자동 이월 여부',

    -- 외래 키= todolist는 user 테이블의 user_id를 참조
                          CONSTRAINT fk_todo_user
                              FOREIGN KEY (user_id) REFERENCES user (user_id)
                                  ON DELETE CASCADE, -- 사용자 탈퇴 시 해당 사용자의 모든 할 일 삭제

    -- 외래 키= todolist는 todofolder 테이블의 folder_id를 참조
                          CONSTRAINT fk_todo_folder
                              FOREIGN KEY (user_id, folder_id) REFERENCES todofolder (user_id, folder_id)
                                  ON DELETE CASCADE -- 폴더 삭제 시 해당 폴더의 모든 할 일 삭제
) COMMENT '할 일 목록 테이블';

CREATE INDEX idx_user_folder_order ON todolist (user_id, folder_id, order_index);
CREATE INDEX idx_due_date ON todolist (due_date);

/* SubTodoList */
CREATE TABLE subtodolist (
                             id INT PRIMARY KEY NOT NULL AUTO_INCREMENT COMMENT '하위 할 일 항목 고유 ID',
                             todolist_id INT NOT NULL COMMENT '상위 할 일 항목 ID (FK)',
                             title VARCHAR(255) NOT NULL COMMENT '하위 할 일 내용',

    -- 외래 키= subtodolist는 todolist 테이블의 todo_id를 참조
                             CONSTRAINT fk_subtodo_todo
                                 FOREIGN KEY (todolist_id) REFERENCES todolist (todo_id)
                                     ON DELETE CASCADE -- 상위 할 일 삭제 시 하위 할 일 삭제
) COMMENT '하위 할 일 목록 테이블';

CREATE INDEX idx_subtodo_todolist_id ON subtodolist (todolist_id);

/* todo_completion_status */
CREATE TABLE todo_completion_status (
                                          `user_id` VARCHAR(100) NOT NULL,
                                          `completed_count` INT NOT NULL DEFAULT 0,
                                          `last_updated` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                          PRIMARY KEY (`user_id`),

                                          CONSTRAINT fk_completion_user
                                          FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
                                          ON DELETE CASCADE

) COMMENT '투두 완료 카운트';

-- REPLACE INTO user 구문 뒤에 추가할 코드 (트리거 대체)
INSERT INTO todofolder (user_id, folder_id, name, is_default)
VALUES ('user01', 1, '기본 폴더', 1),
       ('user01', 999, 'NotTodoList', 1)
ON DUPLICATE KEY UPDATE name = VALUES(name);
-- 1. todofolder 테이블 데이터 삽입
INSERT INTO todofolder (folder_id, user_id, name) VALUES
    (2, 'user01', '개인'),
    (3, 'user01', '업무')
ON DUPLICATE KEY UPDATE name = VALUES(name);


-- 2. todolist 테이블 데이터 삽입 (Todo 항목)
-- 참고: folder_id 1과 999는 트리거를 통해 생성된 '기본 폴더', 'NotTodoList'를 사용한다고 가정합니다.
INSERT INTO todolist (user_id, folder_id, title, td_fixed, is_completed, due_date, order_index, create_date, repeat_days, auto_migrate)
VALUES
    -- Todo 1: 고정된 미완료 할 일 (기본 폴더)
    ('user01', 1, '긴급 보고서 작성', 1, 0, NOW() + INTERVAL 1 DAY, 0, NOW(), NULL, 1),

    -- Todo 2: 완료된 할 일 (업무 폴더)
    ('user01', 3, '팀 회의 자료 검토', 0, 1, NOW() - INTERVAL 1 HOUR, 1, NOW() - INTERVAL 1 DAY, NULL, 0),

    -- Todo 3: 반복 설정된 할 일 (개인 폴더, 월/수/금 반복)
    ('user01', 2, '헬스장 방문 (반복)', 0, 0, NOW() + INTERVAL 1 DAY, 2, NOW(), '0,2,4', 1),

    -- Todo 4: 마감일 없는 할 일 (기본 폴더)
    ('user01', 1, '주간 목표 설정 검토', 0, 0, NULL, 3, NOW(), NULL, 0),

    -- Todo 5: 미완료 할 일 (NotTodo 폴더)
    ('user01', 999, '핸드폰 게임 3시간 하기', 0, 0, NOW() + INTERVAL 3 DAY, 4, NOW(), NULL, 0);


-- 3. subtodolist 테이블 데이터 삽입 (하위 Todo 항목)
-- 참고: todolist의 auto_increment 값이 1부터 시작한다고 가정하고 todolist_id를 1, 3으로 명시했습니다.
INSERT INTO subtodolist (todolist_id, title)
VALUES
    -- Todo 1의 하위 항목
    (1, '데이터 분석 섹션 완료'),
    (1, '차트 디자인 검토'),

    -- Todo 3의 하위 항목
    (3, '유산소 30분'),
    (3, '근력 운동 40분');


-- 4. todo_completion_status 테이블 데이터 삽입
INSERT INTO todo_completion_status (user_id, completed_count, last_updated)
VALUES
    ('user01', 1, NOW() - INTERVAL 1 DAY)
ON DUPLICATE KEY UPDATE completed_count = VALUES(completed_count);