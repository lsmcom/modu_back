-- 1. subtodolist 테이블 삭제 (가장 하위 테이블)
DROP TABLE IF EXISTS subtodolist;
-- 2. todolist 테이블 삭제
DROP TABLE IF EXISTS todolist;
-- 3. todofolder 테이블 삭제 (가장 상위 테이블)
DROP TABLE IF EXISTS todofolder;

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


-- 1. todofolder 테이블 데이터 삽입
INSERT INTO todofolder (folder_id, user_id, name) VALUES
                                                      (0, 'user01', '전체'),
                                                      (1, 'user01', '기본 폴더'),
                                                      (2, 'user01', '개인'),
                                                      (3, 'user01', '업무'),
                                                      (999, 'user01', 'NotTodo');

-- 2. todolist 테이블 데이터 삽입
INSERT INTO todolist (
    todo_id, user_id, folder_id, title, td_fixed, is_completed, due_date, order_index, create_date, repeat_days, auto_migrate
) VALUES
-- --- 1. 날짜를 부여한 기본 할 일 (2025-11-03 기준) ---
-- 수정된 부분: sub_title 컬럼 제거 및 repeat_days, auto_migrate 컬럼 추가
(1, 'user01', 1, '고정 할 일 (11/05)', 1, 0, '2025-11-05 10:00:00', 0, NOW(), NULL, NULL),
(2, 'user01', 1, '하위 Todo 포함 (11/06)', 0, 0, '2025-11-06 10:00:00', 1, NOW(), NULL, NULL), -- sub_title: '하위 TodoList입니다.' -> subtodolist로 이관
(3, 'user01', 2, '개인 폴더 Todo (11/07)', 1, 0, '2025-11-07 10:00:00', 0, NOW(), NULL, NULL),

-- --- 2. NotTodo 항목 (folder_id: 999) ---
(4, 'user01', 999, 'NotTodoList (11/04)', 0, 0, '2025-11-04 08:00:00', 0, NOW(), NULL, NULL),
(5, 'user01', 999, 'NotTodoList 하위 (11/04)', 0, 0, '2025-11-04 09:00:00', 1, NOW(), NULL, NULL), -- sub_title: '하위 TodoList입니다.' -> subtodolist로 이관

-- --- 3. 미완료/기한 초과 항목 (Overdue) ---
(6, 'user01', 2, '미완료 - 11/02 초과', 0, 0, '2025-11-02 23:59:59', 2, NOW(), NULL, NULL), -- sub_title: '하위 TodoList입니다.' -> subtodolist로 이관
(7, 'user01', 2, '미완료 - 10/31 초과', 0, 0, '2025-10-31 23:59:59', 3, NOW(), NULL, NULL),

-- --- 4. 기타 테스트 항목 (날짜/폴더별) ---
(8, 'user01', 1, '다음 주 할 일 (11/10)', 0, 0, '2025-11-10 16:00:00', 2, NOW(), NULL, NULL),
(9, 'user01', 1, '오늘 할 일 (11/03)', 0, 0, '2025-11-03 10:00:00', 6, NOW(), NULL, NULL), -- sub_title: '날짜 필터링 메인' -> subtodolist로 이관
(10, 'user01', 3, '내일 업무 (11/04)', 0, 0, '2025-11-04 12:00:00', 7, NOW(), NULL, NULL), -- sub_title: '업무 폴더 필터' -> subtodolist로 이관
(11, 'user01', 2, '금요일 할 일 (11/07)', 1, 0, '2025-11-07 14:00:00', 8, NOW(), NULL, NULL),
(12, 'user01', 1, '주말 할 일 (11/08)', 0, 0, '2025-11-08 09:00:00', 9, NOW(), NULL, NULL),
(13, 'user01', 1, '다음 주 월요일 (11/10)', 0, 0, '2025-11-10 16:00:00', 10, NOW(), NULL, NULL),
(14, 'user01', 3, '10월 말 업무 (10/30)', 0, 0, '2025-10-30 10:00:00', 11, NOW(), NULL, NULL);

-- 3. subtodolist 테이블 데이터 삽입 (기존 sub_title 데이터를 기반으로 새 항목 생성)
-- todo_id 2, 5, 6, 9, 10의 sub_title 데이터를 title로 이관
INSERT INTO subtodolist (todolist_id, title) VALUES
                                                 (2, '하위 TodoList입니다.'), -- todo_id 2
                                                 (5, '하위 TodoList입니다.'), -- todo_id 5
                                                 (6, '하위 TodoList입니다.'), -- todo_id 6
                                                 (9, '날짜 필터링 메인'),     -- todo_id 9
                                                 (10, '업무 폴더 필터');      -- todo_id 10