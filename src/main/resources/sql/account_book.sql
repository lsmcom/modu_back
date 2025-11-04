/* 가계부 */
create table account_book (

    account_id int not null auto_increment comment '가계부 아이디',
    user_id varchar(100) not null comment '회원 아이디',
    category_id int comment '카테고리 아이디',
    goal_id int comment '저축목표 아이디',
    
    type enum('income','expense') not null comment '수입/지출 타입',
    date date not null comment '날짜',
    method enum('bank','card','cash','etc') comment '결제수단',
    amount int not null comment '금액',
    content varchar(255) comment '내용',
    create_at datetime default now() comment '생성일',
    update_at datetime default null comment '수정일',
    
    primary key(account_id),
    constraint user_account_fk foreign key(user_id) references user(user_id),
    constraint category_account_fk foreign key(category_id) references category(category_id),
    constraint goal_account_fk foreign key(goal_id) references savings_goal(goal_id)
    
) comment '가계부 테이블';


-- 카테고리 테이블 (기본 + 사용자 정의 통합)
create table category (
    category_id int not null auto_increment comment '카테고리 아이디',
    user_id varchar(100) comment '회원 아이디 (기본 카테고리는 null)',
    
    category_name varchar(50) not null comment '카테고리명',
    type enum('income','expense') not null comment '수입/지출 구분',
    color varchar(20) comment '색상',
    is_default boolean default false comment '기본 카테고리 여부',
    
    primary key(category_id),
    constraint fk_category_user foreign key(user_id) references user(user_id)
) comment '카테고리 테이블 (기본 + 사용자 정의)';


/* 저축목표 테이블 */
create table savings_goal (
    goal_id int not null auto_increment comment '저축목표 아이디',
    user_id varchar(100) not null comment '회원 아이디',

    goal_name varchar(100) not null comment '목표명',
    target_amount int not null comment '목표금액',
    current_amount int default 0 comment '현재금액',
    start_date date comment '시작일',
    end_date date comment '목표일',
    
    primary key(goal_id),
    constraint user_goal_fk foreign key(user_id) references user(user_id)
) comment '저축목표 테이블';


/* 예산 테이블 */
create table budget (
    budget_id int not null auto_increment comment '예산 아이디',
    user_id varchar(100) not null comment '회원 아이디',
    category_id int comment '카테고리 아이디',

    `year_month` varchar(7) not null comment '예산 년월 (YYYY-MM)',
    budget_amount int not null comment '예산 금액',
    
    primary key(budget_id),
    constraint user_budget_fk foreign key(user_id) references user(user_id),
    constraint category_budget_fk foreign key(category_id) references category(category_id),
    unique key unique_budget (user_id, category_id, `year_month`)
) comment '예산 테이블';


/* 반복 설정 테이블 */
create table recurring_setting (
    recurring_id int not null auto_increment comment '반복 아이디',
    account_id int not null comment '가계부 아이디',

    cycle enum('daily','weekly','monthly','yearly') not null comment '반복 주기',
    start_date date not null comment '반복 시작일',
    end_date date comment '반복 종료일',
    is_active boolean default true comment '활성 여부',
    days_of_week varchar(20) comment '1,3,5',
    next_date date comment '다음 반복일 계산용',

    primary key(recurring_id),
    constraint account_recurring_fk foreign key(account_id) references account_book(account_id) on delete cascade
) comment '반복 설정 테이블';

/* 할부 설정 테이블 */
create table installment_setting (
    installment_id int not null auto_increment comment '할부 아이디',
    account_id int not null comment '가계부 아이디',

    total_amount int not null comment '총 할부 금액',
    total_months int not null comment '총 할부 개월',
    current_month int not null comment '현재 회차',
    monthly_amount int not null comment '월 납입액',
    start_date date not null comment '할부 시작일',

    primary key(installment_id),
    constraint account_installment_fk foreign key(account_id) references account_book(account_id) on delete cascade
) comment '할부 설정 테이블';

/* 가계부-파일 매핑 테이블 */
create table account_file_mapping (
    mapping_id int not null auto_increment comment '매핑 아이디',
    account_id int not null comment '가계부 아이디',
    file_id varchar(255) not null comment '파일 아이디',
    
    primary key(mapping_id),
    constraint account_file_fk foreign key(account_id) references account_book(account_id) on delete cascade,
    constraint file_account_fk foreign key(file_id) references file(file_id) on delete cascade
) comment '가계부 첨부파일 매핑 테이블';

insert into category (category_name, type, color, is_default)
values ('급여', 'income', '#66BB6A', true),
       ('용돈', 'income', '#4DB6AC', true),
       ('저축', 'income', '#5C6BC0', true),
       ('식비', 'expense', '#4BC0FF', true),
       ('교통비', 'expense', '#FF6384', true),
       ('쇼핑', 'expense', '#FF7043', true),
       ('여가', 'expense', '#FFCE56', true),
       ('운동', 'expense', '#AB47BC', true);

-- ------------------------------------------------------------ 여기부터 추가
ALTER TABLE account_book
    MODIFY COLUMN type ENUM('INCOME','EXPENSE') NOT NULL;

ALTER TABLE account_book
    MODIFY COLUMN method ENUM('BANK','CARD','CASH','ETC');

-- 9월 데이터
INSERT INTO account_book (user_id, category_id, goal_id, type, date, method, amount, content)
VALUES
    ('user01', 1, NULL, 'INCOME', '2025-09-01', 'BANK', 1800000, '월급 입금'),
    ('user01', 4, NULL, 'EXPENSE', '2025-09-02', 'CARD', 25000, '점심 식사'),
    ('user01', 6, NULL, 'EXPENSE', '2025-09-05', 'CARD', 89000, '가을옷 쇼핑'),
    ('user01', 7, NULL, 'EXPENSE', '2025-09-07', 'BANK', 45000, '영화관 데이트'),
    ('user01', 8, NULL, 'EXPENSE', '2025-09-09', 'BANK', 69000, '헬스장 등록'),
    ('user01', 5, NULL, 'EXPENSE', '2025-09-10', 'CASH', 3400, '버스비'),
    ('user01', 3, NULL, 'INCOME', '2025-09-15', 'BANK', 150000, '적금 입금'),
    ('user01', 7, NULL, 'EXPENSE', '2025-09-20', 'CARD', 57000, '놀이공원 이용료'),
    ('user01', 4, NULL, 'EXPENSE', '2025-09-25', 'CASH', 12000, '카페 간식'),
    ('user01', 6, NULL, 'EXPENSE', '2025-09-28', 'CARD', 45000, '책 구입');

-- 10월 데이터
INSERT INTO account_book (user_id, category_id, goal_id, type, date, method, amount, content)
VALUES
    ('user01', 1, NULL, 'INCOME', '2025-10-01', 'BANK', 1800000, '월급 입금'),
    ('user01', 4, NULL, 'EXPENSE', '2025-10-02', 'CARD', 23000, '점심 식사'),
    ('user01', 8, NULL, 'EXPENSE', '2025-10-05', 'BANK', 61000, '클라이밍장 이용'),
    ('user01', 6, NULL, 'EXPENSE', '2025-10-08', 'CARD', 99000, '자켓 구입'),
    ('user01', 5, NULL, 'EXPENSE', '2025-10-10', 'CASH', 4800, '지하철 요금'),
    ('user01', 3, NULL, 'INCOME', '2025-10-15', 'BANK', 100000, '적금 입금'),
    ('user01', 7, NULL, 'EXPENSE', '2025-10-17', 'CARD', 32000, '보드게임 카페'),
    ('user01', 8, NULL, 'EXPENSE', '2025-10-20', 'BANK', 45000, '헬스장 등록'),
    ('user01', 4, NULL, 'EXPENSE', '2025-10-23', 'CARD', 28000, '저녁 식사'),
    ('user01', 6, NULL, 'EXPENSE', '2025-10-29', 'BANK', 75000, '가방 구입');

-- 11월 데이터
INSERT INTO account_book (user_id, category_id, goal_id, type, date, method, amount, content)
VALUES
    ('user01', 1, NULL, 'INCOME', '2025-11-01', 'BANK', 1800000, '월급 입금'),
    ('user01', 4, NULL, 'EXPENSE', '2025-11-02', 'CARD', 27000, '점심 식사'),
    ('user01', 5, NULL, 'EXPENSE', '2025-11-03', 'CASH', 2400, '버스 요금'),
    ('user01', 7, NULL, 'EXPENSE', '2025-11-05', 'BANK', 64000, '공연 티켓'),
    ('user01', 8, NULL, 'EXPENSE', '2025-11-08', 'BANK', 69000, '헬스장 등록'),
    ('user01', 3, NULL, 'INCOME', '2025-11-10', 'BANK', 150000, '적금 입금'),
    ('user01', 4, NULL, 'EXPENSE', '2025-11-15', 'CARD', 34000, '외식'),
    ('user01', 6, NULL, 'EXPENSE', '2025-11-20', 'BANK', 120000, '겨울코트 구입'),
    ('user01', 7, NULL, 'EXPENSE', '2025-11-22', 'CARD', 45000, '영화관 데이트'),
    ('user01', 7, NULL, 'EXPENSE', '2025-11-22', 'CARD', 36000, '영화관 데이트'),
    ('user01', 5, NULL, 'EXPENSE', '2025-11-25', 'CASH', 3600, '버스비');
