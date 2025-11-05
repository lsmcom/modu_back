/* 투두 */
CREATE TABLE todolist (
                          todo_id INT PRIMARY KEY NOT NULL AUTO_INCREMENT COMMENT '할 일 항목 고유 ID',
                          user_id VARCHAR(255) NOT NULL COMMENT '사용자 ID (FK)',
                          folder_id INT NOT NULL COMMENT '폴더 ID (FK)',
                          title VARCHAR(255) NOT NULL COMMENT '할 일 제목',
                          sub_title TEXT NULL COMMENT '할 일 부제/상세 내용',
                          td_fixed TINYINT(1) NOT NULL DEFAULT 0 COMMENT '고정 핀 상태',
                          is_completed TINYINT(1) NOT NULL DEFAULT 0 COMMENT '완료 상태',
                          due_date DATETIME NULL COMMENT '마감일',
                          order_index INT NOT NULL COMMENT '정렬 순서 인덱스',
                          create_date DATETIME NOT NULL COMMENT '생성 일시',

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

/* 투두 폴더 */
CREATE TABLE todofolder (
                            folder_id INT PRIMARY KEY NOT NULL COMMENT '폴더 고유 ID',
                            user_id VARCHAR(255) NOT NULL COMMENT '사용자 ID (FK)',
                            name VARCHAR(255) NOT NULL COMMENT '폴더 이름',

    -- 외래 키 정의: todofolder는 user 테이블의 user_id를 참조
                            CONSTRAINT fk_folder_user
                                FOREIGN KEY (user_id) REFERENCES user (user_id)
                                    ON DELETE CASCADE -- 사용자 탈퇴 시 해당 사용자의 모든 폴더 삭제
) COMMENT '할 일 폴더 테이블';

CREATE INDEX idx_folder_user_id ON todofolder (user_id);


-- 1. todofolder 테이블 데이터 삽입
-- initialMockFolders 및 NotTodo 폴더 (ID: 999) 삽입
INSERT INTO todofolder (folder_id, user_id, name) VALUES
                                                      (0, 'user01', '전체'),
                                                      (1, 'user01', '기본 폴더'),
                                                      (2, 'user01', '개인'),
                                                      (3, 'user01', '업무'),
                                                      (999, 'user01', 'NotTodo');

-- 2. todolist 테이블 데이터 삽입
-- initialTodos 배열 데이터 삽입
INSERT INTO todolist (
    todo_id, user_id, folder_id, title, sub_title, td_fixed, is_completed, due_date, order_index, create_date
) VALUES
-- --- 1. 날짜를 부여한 기본 할 일 (2025-11-03 기준) ---
(1, 'user01', 1, '고정 할 일 (11/05)', NULL, 1, 0, '2025-11-05 10:00:00', 0, NOW()),
(2, 'user01', 1, '하위 Todo 포함 (11/06)', '하위 TodoList입니다.', 0, 0, '2025-11-06 10:00:00', 1, NOW()),
(3, 'user01', 2, '개인 폴더 Todo (11/07)', NULL, 1, 0, '2025-11-07 10:00:00', 0, NOW()),

-- --- 2. NotTodo 항목 (folder_id: 999) ---
(4, 'user01', 999, 'NotTodoList (11/04)', NULL, 0, 0, '2025-11-04 08:00:00', 0, NOW()),
(5, 'user01', 999, 'NotTodoList 하위 (11/04)', '하위 TodoList입니다.', 0, 0, '2025-11-04 09:00:00', 1, NOW()),

-- --- 3. 미완료/기한 초과 항목 (Overdue) ---
(6, 'user01', 2, '미완료 - 11/02 초과', '하위 TodoList입니다.', 0, 0, '2025-11-02 23:59:59', 2, NOW()),
(7, 'user01', 2, '미완료 - 10/31 초과', NULL, 0, 0, '2025-10-31 23:59:59', 3, NOW()),

-- --- 4. 기타 테스트 항목 (날짜/폴더별) ---
(8, 'user01', 1, '다음 주 할 일 (11/10)', NULL, 0, 0, '2025-11-10 16:00:00', 2, NOW()),
(9, 'user01', 1, '오늘 할 일 (11/03)', '날짜 필터링 메인', 0, 0, '2025-11-03 10:00:00', 6, NOW()),
(10, 'user01', 3, '내일 업무 (11/04)', '업무 폴더 필터', 0, 0, '2025-11-04 12:00:00', 7, NOW()),
(11, 'user01', 2, '금요일 할 일 (11/07)', NULL, 1, 0, '2025-11-07 14:00:00', 8, NOW()),
(12, 'user01', 1, '주말 할 일 (11/08)', NULL, 0, 0, '2025-11-08 09:00:00', 9, NOW()),
(13, 'user01', 1, '다음 주 월요일 (11/10)', NULL, 0, 0, '2025-11-10 16:00:00', 10, NOW()),
(14, 'user01', 3, '10월 말 업무 (10/30)', NULL, 0, 0, '2025-10-30 10:00:00', 11, NOW());