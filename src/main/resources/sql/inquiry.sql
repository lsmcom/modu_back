DROP TABLE IF EXISTS inquiry_reply;
DROP TABLE IF EXISTS inquiry;



/* inquiry 테이블 수정 */
CREATE TABLE `inquiry` (
                           `inquiry_id`	BIGINT	NOT NULL AUTO_INCREMENT	COMMENT '문의사항 고유 ID (PK)',
                           `user_id`	varchar(100)	NOT NULL	COMMENT '문의 작성자 ID (FK)',
                           `title`	VARCHAR(255)	NOT NULL	COMMENT '문의 제목',
                           `content`	TEXT	NOT NULL	COMMENT '문의 내용',
                           `status`	ENUM('temp', 'submitted', 'answered')	NOT NULL	DEFAULT 'submitted'	COMMENT '문의 상태',
                           `is_public`	BOOLEAN	NOT NULL	DEFAULT FALSE	COMMENT 'FAQ 공개 여부',
                           `create_at`	DATETIME	NOT NULL	DEFAULT now()	COMMENT '작성일',
                           `update_at`	DATETIME	NULL	DEFAULT NULL	COMMENT '수정일',

                           PRIMARY KEY(`inquiry_id`),
                           CONSTRAINT fk_inquiry_user FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) COMMENT '문의 테이블';

/* inquiry_reply */
CREATE TABLE `inquiry_reply` (
                                 `reply_id`	BIGINT	NOT NULL AUTO_INCREMENT	COMMENT '답변 고유 ID (PK)',
                                 `inquiry_id`	BIGINT	NOT NULL	COMMENT '문의사항 ID (FK)',
                                 `admin_id`	VARCHAR(100)	NOT NULL	COMMENT '답변 관리자 ID (FK)',
                                 `content`	TEXT	NOT NULL	COMMENT '답변 내용',
                                 `create_at`	DATETIME	NOT NULL	DEFAULT now()	COMMENT '생성일 (BaseTimeEntity)',
                                 `update_at`	DATETIME	NULL	DEFAULT NULL	COMMENT '수정일 (BaseTimeEntity)',

                                 PRIMARY KEY(`reply_id`),
                                 CONSTRAINT fk_reply_inquiry FOREIGN KEY (`inquiry_id`) REFERENCES `inquiry` (`inquiry_id`),
                                 CONSTRAINT fk_reply_admin FOREIGN KEY (`admin_id`) REFERENCES `user` (`user_id`)
) COMMENT '문의 답변 테이블';


/* 11.13 수정 */
/* 문의사항 사용자 목데이터 */
INSERT INTO inquiry (user_id, title, content, status, is_public, create_at)
VALUES
    ('user01', 'TodoList 완료 체크가 저장되지 않습니다',
     '오늘 완료한 할 일을 체크해도 새로고침하면 다시 미완료로 돌아갑니다. 확인 부탁드립니다.',
     'submitted', FALSE,
     DATE_ADD(DATE_SUB(NOW(), INTERVAL FLOOR(RAND()*14) DAY), INTERVAL FLOOR(RAND()*86400) SECOND)
    ),

    ('user01', '알림이 일정 시간 지나면 울리지 않습니다',
     '설정해둔 TodoList 알림이 가끔 누락됩니다. 특정 시간대에만 이런 현상이 발생하는 것 같습니다.',
     'submitted', FALSE,
     DATE_ADD(DATE_SUB(NOW(), INTERVAL FLOOR(RAND()*14) DAY), INTERVAL FLOOR(RAND()*86400) SECOND)
    ),

    ('user02', '가계부에서 카드 내역 자동 연동이 안 됩니다',
     '카드 사용 내역을 불러오려고 하면 오류 메시지가 뜹니다. 계정 연동 문제일까요?',
     'submitted', FALSE,
     DATE_ADD(DATE_SUB(NOW(), INTERVAL FLOOR(RAND()*14) DAY), INTERVAL FLOOR(RAND()*86400) SECOND)
    ),

    ('user03', '캘린더 일정이 다른 날짜로 이동하는 현상이 있어요',
     '일정을 추가하면 다음날로 자동 이동됩니다. 타임존 문제인지 확인 부탁드립니다.',
     'submitted', FALSE,
     DATE_ADD(DATE_SUB(NOW(), INTERVAL FLOOR(RAND()*14) DAY), INTERVAL FLOOR(RAND()*86400) SECOND)
    ),

    ('user03', '메모 작성 후 저장 버튼을 눌러도 저장이 되지 않습니다',
     '짧은 메모는 저장되는데 긴 문장은 저장 실패가 뜹니다. 글자 수 제한이 있는지 궁금합니다.',
     'submitted', FALSE,
     DATE_ADD(DATE_SUB(NOW(), INTERVAL FLOOR(RAND()*14) DAY), INTERVAL FLOOR(RAND()*86400) SECOND)
    ),

    ('user04', '프로필 이미지가 업로드되지 않아요',
     'JPG 파일을 올리면 업로드 실패라고 나오고 PNG만 됩니다. 파일 용량 문제는 아닙니다.',
     'submitted', FALSE,
     DATE_ADD(DATE_SUB(NOW(), INTERVAL FLOOR(RAND()*14) DAY), INTERVAL FLOOR(RAND()*86400) SECOND)
    ),

    ('user05', '가계부 통계 그래프가 표시되지 않습니다',
     '수입/지출을 입력했는데 통계 페이지에서 그래프가 비어 있습니다.',
     'submitted', FALSE,
     DATE_ADD(DATE_SUB(NOW(), INTERVAL FLOOR(RAND()*14) DAY), INTERVAL FLOOR(RAND()*86400) SECOND)
    ),

    ('user05', '로그인 유지가 자주 풀립니다',
     '로그인 유지 체크를 했는데 하루 정도 지나면 다시 로그인해야 합니다. 세션 관련 문제로 보입니다.',
     'submitted', FALSE,
     DATE_ADD(DATE_SUB(NOW(), INTERVAL FLOOR(RAND()*14) DAY), INTERVAL FLOOR(RAND()*86400) SECOND)
    );

/* 문의사항 관리자 목데이터 */
INSERT INTO inquiry (user_id, title, content, status, is_public)
VALUES
    ('admin01',
     '문의 작성 규칙 안내',
     '고객센터 문의 작성 시 제목은 핵심 내용을 포함해 작성해주시고, 상황 설명은 가능한 상세히 작성해주시기 바랍니다.
     필요시 스크린샷 또는 오류 발생 시점 등을 함께 제공해주시면 더 빠르게 처리할 수 있습니다.
     부적절한 표현이나 욕설은 제재 사유가 될 수 있으니 유의해 주세요.',
     'answered',
     TRUE
    ),

    ('admin02',
     '자주 묻는 질문 모음',
     '아래는 사용자들이 자주 문의하는 기능 관련 질문을 모아 정리한 내용입니다.

    [1] TodoList가 저장되지 않아요.
     - 브라우저 새로고침 후 체크 상태가 초기화된다면 네트워크 불안정 또는 캐싱 문제일 수 있습니다.
     - 앱 버전이 오래되었을 경우 업데이트 후 다시 시도해주세요.

    [2] 가계부 통계 그래프가 보이지 않습니다.
     - 수입/지출 항목이 월별 기준으로 최소 1개 이상 등록되어 있어야 그래프가 생성됩니다.
     - 그래도 표시되지 않는다면 브라우저 캐시 삭제 후 다시 시도해보세요.

    [3] 캘린더 일정이 다른 날짜로 이동해요.
     - 시작일과 종료일 설정 시 시간대(타임존) 문제가 있을 수 있습니다.
     - 기기 시간 설정이 자동 동기화인지 확인해주세요.

    [4] 메모 저장이 되지 않습니다.
     - 메모는 텍스트 기반으로 글자 수 제한은 없지만, 네트워크 전송 중 실패할 경우 저장이 되지 않을 수 있습니다.
     - 장문의 메모일 경우 임시 저장 후 등록을 권장합니다.

    [5] 알림이 울리지 않아요.
     - 기기 알림 권한이 허용되어 있는지 확인해주세요.
     - 절전 모드나 배터리 최적화가 켜져 있으면 알림이 차단될 수 있습니다.

    [6] 로그인 유지가 자주 풀립니다.
     - 보안 정책상 일정 시간이 지나면 자동 로그아웃될 수 있습니다.
     - 너무 자주 풀릴 경우 쿠키 삭제 또는 브라우저 설정 확인이 필요합니다.',
     'answered',
     TRUE
    );

/* 문의사항 - 파일 매핑 테이블 생성 */
CREATE TABLE inquiry_file_mapping (
    inquiry_file_mapping_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '문의사항-파일 매핑 ID (PK)',
    inquiry_id BIGINT NOT NULL COMMENT '문의사항 ID (FK)',
    file_id VARCHAR(255) NOT NULL COMMENT '파일 ID (FK)',
    created_at DATETIME DEFAULT NOW() COMMENT '등록일',

    PRIMARY KEY (inquiry_file_mapping_id),

    CONSTRAINT fk_inquiry_file_mapping_inquiry
        FOREIGN KEY (inquiry_id) REFERENCES inquiry(inquiry_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_inquiry_file_mapping_file
        FOREIGN KEY (file_id) REFERENCES file(file_id)
        ON DELETE CASCADE
) COMMENT '문의사항-파일 매핑 테이블';
