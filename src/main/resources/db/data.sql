-- 코드테이블 
-- 유저 구분 코드 테이블 
INSERT INTO user_role_code(user_role) VALUES ('운영자'), ('관리자'),('선생님');

-- 지역 코드 테이블
INSERT INTO region_code (region, code) 
VALUES ('서울', 'SEO'), ('부산', 'PUS'), ('대구', 'DAE'), ('인천', 'INC'), ('광주', 'GWJ'), ('대전', 'DTJ'), ('울산', 'ULS'), 
       ('세종', 'SJG'), ('경기', 'GGD'), ('경상', 'GND'), ('경북', 'GBD'), ('전남', 'JND'), ('전북', 'JBD'), ('충남', 'CND'),
       ('충북', 'CBD'), ('강원', 'GWD'), ('제주', 'JJD');

INSERT INTO relation_code(relation) VALUES ('부'), ('모'), ('기타');

-- 센터 코드 테이블
INSERT INTO center_tb(center_no, center_name, opend_at, biz_no, ceo_name, tel, center_mail, region_no) 
VALUES ('PUS001', '본사', '2025-06-25', '0000000000', '임무화', '07052226240','st8898ds@gmail.com', 2), 
       ('DAE001', '월성점','2025-06-25', '0000000000', '황아름','0532628898', 'st8898ds@gmail.com', 3),
       ('PUS002', '남천점','2025-06-25', '0000000000', '남지영','0516288898', 'st8898ds@gmail.com', 2),
       ('ULS001', '유곡점', '2025-06-25', '0000000000', '우완주','0516288898', 'st8898@gmail.com', 7);

-- 학년 코드 테이블
INSERT INTO grade_code(grade) VALUES ('5세'), ('6세'), ('7세'), ('초1'), ('초2'), ('초3'), ('초4'), ('초5');

-- 급수 코드 테이블
INSERT INTO level_code(level) VALUES ('8급'), ('7급'), ('6급'), ('5급'), ('4급'), ('3급'), ('2급'), ('1급');

-- 상태 코드 테이블
INSERT INTO status_code (code, status) VALUES ('001', '재원중'), ('002', '입회취소'), ('003', '휴원'), ('004', '탈퇴');

INSERT INTO unit_code(unit_code) VALUES ('1호'), ('2호'), ('3호'), ('4호'), ('5호'), ('6호'), ('7호'), ('8호'), ('9호'), ('10호');

-- 수업 코드 테이블 
INSERT INTO class_code (class_name, class_type, center_no)
VALUES ('초등천재', '1', 'DAE001'), ('초등수재', '1', 'DAE001'), ('초등박사','1','DAE001'), ('초등신동','1','DAE001'), ('한스쿨I','1','DAE001'), ('한스쿨II','1','DAE001'), ('호호하니','1','DAE001'),
       ('새움', '2','DAE001'), ('다움', '2','DAE001'), ('티움', '2','DAE001'), ('채움', '2','DAE001'), ('북스쿨', '2','DAE001'), ('호호부키', '2','DAE001');

 
INSERT INTO user_tb(user_id, password, username, user_role_no, center_no, created_at)  
VALUES ('ssar', '1234','김하나', 1, 'PUS001', now()), 
       ('cos', '1234', '이하나',2, 'DAE001', now()), 
       ('mango', '1234', '박하나',2, 'DAE001', now()),
       ('love', '1234', '최하나', 2, 'DAE001', now()); 

INSERT INTO student_tb (student_id, student_name, birth, gender, school, address, address_detail, entry_han_date, entry_book_date, created_at, grade_no, level_no, status_no, center_no, student_privacy_agree)  
VALUES ('abc001','김호호', '2018-08-15', true, '호호유치원','부산광역시 해운대구 센텀중앙로97', '센텀스카이비즈 A동 2810호', '2025-06-12','2025-06-12', now(), 1, 3, 1, 'PUS002', true), 
       ('abc002', '김호일', '2017-07-15', true, '호호초등학교','부산광역시 해운대구 센텀중앙로96', '센텀스카이비즈 A동 2809호', '2025-06-11','2025-06-11', now(), 2, 4, 1, 'DAE001', true), 
       ('abc003', '김호이', '2016-06-15', false, '호호초등학교','부산광역시 해운대구 센텀중앙로95', '센텀스카이비즈 A동 2808호', '2025-06-10','2025-06-10', now(), 3, 5, 1, 'PUS002', true),
       ('abc004', '김호삼', '2016-06-15', false, '호호초등학교','부산광역시 해운대구 센텀중앙로95', '센텀스카이비즈 A동 2808호', NULL,NULL, now(), 2, 2, 1, NULL, true);

INSERT INTO class_instance(class_no, unit_no, user_no) VALUES(1,1, 2),(8,1,3),(2,1,2),(9,1,3),(1,1,2);

INSERT INTO user_class(user_no, class_instance_no)
VALUES (2, 1), (3, 2), (2, 3), (1, 4),(2, 5);

INSERT INTO student_class (student_no, class_instance_no) 
VALUES (1, 1), (1, 2), (2, 3), (2, 4), (3, 5);

INSERT INTO student_transfer_history (student_no, from_user_no, to_user_no, class_no, transfer_reason, move_at, created_at)
VALUES (1, 2, 3, 1, '인원 미달', '2025-07-06', now()),
       (1, 3, 2, 1, '인원 미달', '2025-07-06', now()),
       (1, 2, 3, 1, '인원 미달', '2025-07-06', now()),
       (1, 3, 2, 1, '인원 미달', '2025-07-06', now()),
       (1, 2, 3, 1, '인원 미달', '2025-07-06', now()),
       (2, 2, 3, 1, '인원 미달', '2025-07-06', now());


INSERT INTO status_history (student_no, status_no, reason, created_at)
VALUES (1, 1, NULL, now()),
       (2, 1, NULL, now()),
       (3, 3, '여행으로 인한 1개월 휴원', now()),
       (4, 1, NULL, now());

INSERT INTO sibling(sibling_code, student_no)
VALUES ('20250001', 1), ('20250001', 2);

INSERT INTO time_table(yy, mm, dayname, period_no, start_time, end_time, class_no, unit_no, grade_no, user_no, created_at)
VALUES ('2025', '07', 'mon', 1, '12:50', '13:40', 1, 1, 6, 2, now()),
       ('2025', '07', 'mon', 2, '13:50', '14:40', 9, 1, 6, 2, now()),
       ('2025', '07', 'fri', 5, '12:50', '13:40', 1, 2, 6, 2, now()), 
       ('2025', '07', 'mon', 3, '14:50', '15:40', 2, 2, 7, 2, now()); 

INSERT INTO time_table_assign (student_no, week, time_table_no)
VALUES (1, 4, 1), (2, 4, 1), (3, 4, 1), (4, 3, 1),
       (1, 4, 2), (2, 4, 2), (3, 4, 2), (4, 3, 2);


 

