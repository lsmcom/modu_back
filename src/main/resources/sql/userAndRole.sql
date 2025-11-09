/* modu 데이터 베이스 생성 */
create database modu;

/* 권한 테이블 생성 */
create table user_role(
	role_id		varchar(255)	not null comment '권한 아이디',
	role_name	varchar(255)	not null comment '권한 이름',
	
	primary key(role_id)
)comment '권한 테이블';

/* 회원 테이블 생성 */
create table user(

	user_id 		varchar(100) not null 			comment '회원 아이디',
	password		varchar(255) not null 			comment '회원 비밀번호',
	user_name		varchar(50)  not null 			comment '회원 이름',
	user_nick		varchar(50)  not null 			comment '회원 닉네임',
	email			varchar(100) not null 			comment '회원 이메일',
	birth			date		 not null 			comment '회원 생년월일',
	agency 			varchar(50)  not null 			comment '통신사',
	phone			varchar(50)  not null 			comment '회원 전화번호',
	addr			varchar(255) not null 			comment '회원 주소',
	addr_detail 	varchar(255) default '' 		comment '회원 상세주소',
	create_at		datetime	 default now() 		comment '회원 가입일',
	update_at		datetime	 default null 		comment '회원 수정일',
	withdraw_at 	datetime	 default null		comment '회원 탈퇴일',
	withdraw_reason varchar(255) default null		comment '회원 탈퇴이유',
	social_type		varchar(50)	 default null		comment '회원 소셜 종류(naver, kakao)',
	status			varchar(20)	 default 'active'	comment '회원 상태(ex: 활성화, 정지, 휴먼)',
	user_role		varchar(50)	 default 'USER' 	comment '회원 권한',
	
	primary key(user_id),
	constraint user_rol_fk foreign key(user_role) references user_role(role_id)
)comment '회원 테이블';

/* 권한 테이블에 데이터 삽입 */
insert into user_role(role_id, role_name) values
('ADMIN', '관리자'),
('USER', '사용자');

/* 
 * 회원 테이블 임시 데이터 삽입 
 * 관리자 비밀번호(admin1234)
 * 회원 비밀번호(user1234)
 */
insert into user(
	user_id, password, user_name, user_nick, email, birth, agency, phone, addr, addr_detail, user_role
) values
('admin01', '$2a$12$GqCn8kTGiM6VlL4BtVLBBua2KffFCaImvM2YEo3GmpNPuM9VZVrgm',
'관리자', '관리자','admim01@naver.com', '980123', 'SKT',
'010-1234-5678', '서울 마포구 서강로 136', '신촌IT아카데미 2층', 'ADMIN'),
('user01', '$2a$12$ejj5.Hi18FA6kXYAdM2ORefkX1ksYxoAAVzDeNMgWRyyel1upKWwW',
'사용자', '사용자','user01@naver.com', '901005', 'KT',
'010-9876-5432', '서울 마포구 서강로 136', '신촌IT아카데미 2층', 'USER');

/* 파일 테이블 생성 */
create table file(

	file_id			varchar(255)	not null			comment '파일 번호',
	user_id 		varchar(100) 	not null 			comment '회원 아이디',
	file_type		varchar(100)	not null			comment '파일 타입(PROFILE, POST 등)',
	file_name		varchar(255)	not null			comment '파일 이름',
	stored_name		varchar(255)	not null			comment '파일 저장 이름',
	file_path		varchar(255)	not null			comment	'파일 경로',
	file_size       bigint          not null    		comment '파일 크기 (Byte)',
	file_thumb_name	varchar(255)	null				comment '파일 썸네일 이름',
	create_at		datetime	 	default now() 		comment '파일 생성일',
	update_at		datetime	 	default null 		comment '파일 수정일',
	
	primary key(file_id),
	constraint file_user_fk foreign key(user_id) references user(user_id)

)comment '파일 테이블';

/* 사용자 설정 테이블 */
create table user_setting(

	setting_id				varchar(100)	not null		comment '설정 고유 번호',
	user_id					varchar(100)	not null		comment '회원 아이디',
	the_day_of_week 		char(1)			default 'M' 	comment '주시작일 -> M: 월요일, S: 일요일',
	theme_mode				varchar(20)		default 'light'	comment '테마 모드 -> light: 기본 모드, dark: 다크모드',
	alarm_allowed			char(1)			default 'Y' 	comment '알람 허용 여부 -> Y: 허용, N: 미허용',
	personal_info_agreed	char(1)			default 'Y' 	comment '개인정보 수집/이용 동의 여부 -> Y: 허용, N: 미허용',
	location_info_agreed	char(1)			default 'Y' 	comment '위치기반 서비스 동의 여부 -> Y: 허용, N: 미허용',
	marketing_info_agreed	char(1)			default 'Y' 	comment '마켓팅 정보 수신 동의 여부 -> Y: 허용, N: 미허용',
	marketing_reject_date	datetime		default null	comment '마켓팅 정보 수신 동의 변경 날짜',
	
	primary key(setting_id),
	CONSTRAINT fk_user_setting_user FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE
	
)comment '사용자 설정 테이블';


/* 11.07 추가 sql */
/* 사용자 설정 테이블 기본 데이터 삽입 */
INSERT INTO user_setting (setting_id, user_id, the_day_of_week, theme_mode, alarm_allowed,
	personal_info_agreed, location_info_agreed, marketing_info_agreed, marketing_reject_date)VALUES
(UUID(), 'user01', DEFAULT, DEFAULT, DEFAULT, DEFAULT, DEFAULT, DEFAULT, DEFAULT),
(UUID(), 'user02', DEFAULT, DEFAULT, DEFAULT, DEFAULT, DEFAULT, DEFAULT, DEFAULT),
(UUID(), 'user03', DEFAULT, DEFAULT, DEFAULT, DEFAULT, DEFAULT, DEFAULT, DEFAULT),
(UUID(), 'user04', DEFAULT, DEFAULT, DEFAULT, DEFAULT, DEFAULT, DEFAULT, DEFAULT),
(UUID(), 'user05', DEFAULT, DEFAULT, DEFAULT, DEFAULT, DEFAULT, DEFAULT, DEFAULT);


























