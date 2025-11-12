
/* 커뮤니티 sql */ 
/* 게시판 테이블 */
create table community_board(
	board_id	int				auto_increment	comment '게시판 번호',
	board_name	varchar(100)	not null		comment '게시판 명',
	
	primary key(board_id)
) comment '게시판 테이블';

/* 게시판 데이터 */
insert into community_board(board_name) values
("공지사항"), ("투두 실천 챌린지"), ("절약 / 가게부 팁"),
("메모 공개함"), ("일정 공유 게시판"), ("질문 / 답변 게시판"), ("자유 게시판");

/* 커뮤니티 게시글 */
create table community_post (
    post_id         int             auto_increment        comment '게시글 번호',
    board_id        int             null              	  comment '게시판 번호(임시저장은 null가능)',
    user_id         varchar(100)    not null              comment '회원 아이디',
    title           varchar(50)     default null          comment '게시글 제목',
    contents        text            default null          comment '게시글 내용',
    create_at       datetime        default now()         comment '게시글 작성일',
    update_at       datetime        default null          comment '게시글 수정일',
    read_count      int             default 0             comment '게시글 조회수',
    like_count      int             default 0             comment '게시글 추천수 (캐시용)',
    is_temporary    char(1)         default 'N'           comment '게시글 임시저장 여부 -> Y: 임시저장, N: 미임시저장',

    primary key (post_id),
    constraint fk_post_board foreign key (board_id) references community_board(board_id),
    constraint fk_post_user foreign key (user_id) references user(user_id)
) comment '커뮤니티 게시글';

/* 게시글 - 파일 매핑 테이블 */
create table community_post_file (
    post_id     int             not null        comment '게시글 번호',
    file_id     varchar(255)    not null        comment '파일 번호',
    file_order  int             default 1       comment '파일 표시 순서 (1부터 시작)',
    file_role   enum('CONTENT', 'ATTACHMENT', 'THUMBNAIL') default 'ATTACHMENT' comment '파일 용도 구분',

    primary key (post_id, file_id),
    constraint fk_postfile_post foreign key (post_id) references community_post(post_id) on delete cascade,
    constraint fk_postfile_file foreign key (file_id) references file(file_id) on delete cascade
) comment '게시글 - 파일 매핑 테이블';

/* 커뮤니티 필독공지(상단 고정) 테이블 */
create table community_fixed_notice (
    fixed_id    int             auto_increment       comment '필독공지 식별자',
    post_id     int             not null             comment '고정된 게시글 번호',
    admin_id    varchar(100)    not null             comment '설정한 관리자 아이디',
    fixed_order tinyint         not null             comment '고정 순서 (1, 2 등)',
    fixed_at    datetime        default now()        comment '고정 설정 시각',
    
    primary key (fixed_id),
    unique key uk_fixed_post (post_id),
    unique key uk_fixed_order (fixed_order),
    constraint fk_fixed_post foreign key (post_id) references community_post(post_id) on delete cascade,
    constraint fk_fixed_admin foreign key (admin_id) references user(user_id) on delete cascade
) comment '커뮤니티 상단 고정 필독공지';

/* 게시글 좋아요 */
create table community_post_like (
    post_id     int             not null                comment '게시글 번호',
    user_id     varchar(100)    not null                comment '좋아요 누른 회원 아이디',
    created_at  datetime        default now()           comment '좋아요 누른 시각',

    primary key (post_id, user_id),
    constraint fk_like_post foreign key (post_id) references community_post(post_id) on delete cascade,
    constraint fk_like_user foreign key (user_id) references user(user_id) on delete cascade
) comment '게시글 좋아요';

/* 게시글 댓글 */
create table community_post_comment (
    comment_id          int             auto_increment       comment '댓글 번호',
    post_id             int             not null             comment '게시글 번호',
    user_id             varchar(100)    not null             comment '댓글 작성 회원 아이디',
    contents            text            not null             comment '댓글 내용',
    create_at           datetime        default now()        comment '댓글 작성일',
    update_at           datetime        default null         comment '댓글 수정일',
    parent_comment_id   int             default null         comment '부모 댓글 번호 (NULL이면 일반 댓글)',

    primary key (comment_id),
    constraint fk_comment_post foreign key (post_id) references community_post(post_id) on delete cascade,
    constraint fk_comment_user foreign key (user_id) references user(user_id) on delete cascade,
    constraint fk_comment_parent foreign key (parent_comment_id) references community_post_comment(comment_id) on delete cascade
) comment '게시글 댓글';

/* 게시글 신고 */
create table community_report (
    report_id       int             auto_increment       comment '신고 번호',
    post_id         int             not null             comment '신고된 게시글 번호',
    user_id         varchar(100)    not null             comment '신고한 회원 아이디',
    report_reason   varchar(255)    not null             comment '신고 사유',
    created_at      datetime        default now()        comment '신고 일시',
    report_status   enum('PENDING', 'APPROVED', 'REJECTED') default 'PENDING' comment '신고 처리 상태',

    primary key (report_id),
    unique key uk_report_post_user (post_id, user_id),
    constraint fk_report_post foreign key (post_id) references community_post(post_id) on delete cascade,
    constraint fk_report_user foreign key (user_id) references user(user_id) on delete cascade
) comment '게시글 신고';

/* 커뮤니티 게시글 설정 */
create table community_post_setting(
	setting_id		int				auto_increment	comment '설정 번호',
	post_id         int             not null        comment '설정된 게시글 번호',
    user_id         varchar(100)    not null        comment '설정한 회원 아이디',
    is_public		char(1)			default 'Y'		comment	'공개 허용 -> Y: 허용, N: 불가',
    is_search		char(1)			default 'Y'		comment	'검색 허용 -> Y: 허용, N: 불가',
    is_comment		char(1)			default 'Y'		comment	'댓글 허용 -> Y: 허용, N: 불가',
    is_in_share		char(1)			default 'Y'		comment	'커뮤 내 공유 허용 -> Y: 허용, N: 불가',
    is_copy			char(1)			default 'Y'		comment	'복사, 저장 허용 -> Y: 허용, N: 불가',
    is_out_share	char(1)			default 'Y'		comment	'외부 공유 허용 -> Y: 허용, N: 불가',
    image_size_type enum('NORMAL', 'LARGE', 'ORIGINAL') default 'LARGE' comment '첨부 이미지 크기 설정 -> NORMAL:640, LARGE:1280, ORIGINAL:원본',
    
    primary key (setting_id),
    constraint fk_setting_post foreign key (post_id) references community_post(post_id) on delete cascade,
    constraint fk_setting_user foreign key (user_id) references user(user_id) on delete cascade
) comment '커뮤니티 게시글 설정';

/* 사용자 차단 테이블 */
create table user_block (
    blocker_id varchar(100) not null comment '차단한 회원 아이디',
    blocked_id varchar(100) not null comment '차단당한 회원 아이디',
    created_at datetime default now() comment '차단한 시각',

    primary key (blocker_id, blocked_id),
    constraint fk_blocker_user foreign key (blocker_id) references user(user_id) on delete cascade,
    constraint fk_blocked_user foreign key (blocked_id) references user(user_id) on delete cascade
) comment '회원 차단 관계';


/* 11.06 수정사항 */
ALTER TABLE community_post DROP FOREIGN KEY fk_post_board;

ALTER TABLE community_post
  MODIFY COLUMN board_id INT NULL;

ALTER TABLE community_post
  ADD CONSTRAINT fk_post_board
  FOREIGN KEY (board_id)
  REFERENCES community_board(board_id)
  ON DELETE SET NULL
  ON UPDATE CASCADE;


/* 11.11 수정사항 */
/* 게시글 조회 이력 */
create table community_post_view (
    view_id     int 			auto_increment 	comment '조회 번호',
    post_id     int 			not null 		comment '조회한 게시글 번호',
    user_id     varchar(100) 	not null 		comment '조회한 사용자 아이디',
    created_at  datetime        default now()   comment '조회한 시각',
    
    primary key (view_id),
    constraint fk_view_post foreign key (post_id) references community_post(post_id),
    constraint fk_view_user foreign key (user_id) references user(user_id),
    unique key uk_post_user (post_id, user_id)
) comment '게시글 조회 이력';

/* 기존 PRIMARY KEY 및 FK 제약조건 제거 */
ALTER TABLE community_post_like
DROP PRIMARY KEY,
DROP FOREIGN KEY fk_like_post,
DROP FOREIGN KEY fk_like_user;

/* like_id 컬럼 추가 및 기본키 설정 */
ALTER TABLE community_post_like
ADD COLUMN like_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '좋아요 고유 ID' FIRST;

/* post_id + user_id 중복 방지 UNIQUE 제약 추가 */
ALTER TABLE community_post_like
ADD CONSTRAINT uq_post_user UNIQUE (post_id, user_id);

/* 외래키 제약 다시 추가 (CASCADE 유지) */
ALTER TABLE community_post_like
ADD CONSTRAINT fk_like_post FOREIGN KEY (post_id) REFERENCES community_post(post_id) ON DELETE CASCADE,
ADD CONSTRAINT fk_like_user FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE;
































