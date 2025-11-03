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
    created_at datetime default now() comment '생성일',
    updated_at datetime default null comment '수정일',
    
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

    year_month varchar(7) not null comment '예산 년월 (YYYY-MM)',
    budget_amount int not null comment '예산 금액',
    
    primary key(budget_id),
    constraint user_budget_fk foreign key(user_id) references user(user_id),
    constraint category_budget_fk foreign key(category_id) references category(category_id),
    unique key unique_budget (user_id, category_id, year_month)
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


