-- 코드테이블
-- 유저 구분 코드 테이블
INSERT INTO erp_user_role_code (role_key, role_name)
VALUES ('ADMIN', '운영자'),
       ('MANAGER', '관리자'),
       ('TEACHER', '선생님');

-- 지역 코드 테이블
INSERT INTO erp_region_code (region_key, region_name)
VALUES ('SEO', '서울'),
       ('PUS', '부산'),
       ('DAE', '대구'),
       ('INC', '인천'),
       ('GWJ', '광주'),
       ('DTJ', '대전'),
       ('ULS', '울산'),
       ('SJG', '세종'),
       ('GGD', '경기'),
       ('GND', '경상'),
       ('GBD', '경북'),
       ('JND', '전남'),
       ('JBD', '전북'),
       ('CND', '충남'),
       ('CBD', '충북'),
       ('GWD', '강원'),
       ('JJD', '제주');

INSERT INTO relation_code(relation)
VALUES ('부'),
       ('모'),
       ('기타');


-- 학년 코드 테이블
INSERT INTO erp_grade_code (grade_key, grade_name)
VALUES ('05', '5세'),
       ('06', '6세'),
       ('07', '7세'),
       ('11', '초1'),
       ('12', '초2'),
       ('13', '초3'),
       ('14', '초4'),
       ('15', '초5');

-- 급수 코드 테이블
INSERT INTO erp_level_code (level_key, level_name)
VALUES ('L8', '8급'),
       ('L7', '7급'),
       ('L6', '6급'),
       ('L6P', '준6급'),
       ('L5P', '준5급'),
       ('L5', '5급'),
       ('L4', '4급'),
       ('L3', '3급'),
       ('L2', '2급'),
       ('L1', '1급');

-- 상태 코드 테이블
INSERT INTO erp_status_code (status_key, status_name)
VALUES ('ACTIVE', '재원중'),
       ('CANCELLED', '입회취소'),
       ('PAUSED', '휴원'),
       ('WITHDRAWN', '탈퇴');

-- 진도 코드 테이블
INSERT INTO erp_unit_code(unit_key, unit_name)
VALUES ('H01', '1호'),
       ('H02', '2호'),
       ('H03', '3호'),
       ('H04', '4호'),
       ('H05', '5호'),
       ('H06', '6호'),
       ('H07', '7호'),
       ('H08', '8호'),
       ('H09', '9호'),
       ('H10', '10호'),
       ('M01', '1개월'),
       ('M02', '2개월'),
       ('M03', '3개월'),
       ('M04', '4개월'),
       ('M05', '5개월'),
       ('M06', '6개월'),
       ('M07', '7개월'),
       ('M08', '8개월'),
       ('M09', '9개월'),
       ('M10', '10개월'),
       ('M11', '11개월'),
       ('M12', '12개월'),
       ('L80', '8급'),
       ('L70', '7급'),
       ('L61', '준6급'),
       ('L60', '6급'),
       ('L51', '준5급'),
       ('L50', '5급');

-- 수업 코드 테이블
INSERT INTO erp_class_code (class_key, class_name, class_type)
VALUES ('C001', '초등천재', '1'),
       ('C002', '초등수재', '1'),
       ('C003', '초등박사', '1'),
       ('C004', '초등신동', '1'),
       ('C005', '한스쿨', '1'),
       ('C006', '영재', '1'),
       ('C007', '수재', '1'),
       ('C008', '신동', '1'),
       ('C009', '새움', '2'),
       ('C0010', '다움', '2'),
       ('C0011', '티움', '2'),
       ('C0012', '채움', '2'),
       ('C0013', '북스쿨', '2'),
       ('C0014', '키움', '2'),
       ('C0015', '만남', '2'),
       ('C0016', '자람', '2');

-- 유입 경로 코드 테이블
INSERT INTO inflow_route (inflow_route)
VALUES ('네이버 검색'),
       ('블로그'),
       ('SNS'),
       ('Youtube');

-- 상담 진행 코드 테이블
INSERT INTO progress_code (progress)
VALUES ('상담진행'),
       ('수업대기'),
       ('수업확정'),
       ('종료');

-- 특이사항 카테고리 테이블
INSERT INTO erp_remark_category (remark_category_key, remark_category_name)
VALUES ('PREP', '수업 준비'),
       ('ATT', '수업 태도'),
       ('UNDER', '이해도');

-- 특이사항 코드 테이블
INSERT INTO erp_remark_code (remark_key, remark_name, remark_category_key)
VALUES ('PREP_NOBOOK', '수업도서 안 읽어옴', 'PREP'),
       ('PREP_NOHW', '숙제 안함', 'PREP'),
       ('PREP_NOMAT', '교재준비 안함', 'PREP'),
       ('PREP_ETC', '기타준비 안함', 'PREP'),
       ('ATT_LOW_PART', '발표 참여도 낮음', 'ATT'),
       ('ATT_DISTRACT', '산만함', 'ATT'),
       ('ATT_INTERRUPT', '수업방해', 'ATT'),
       ('ATT_LOSSMOT', '의욕저하', 'ATT'),
       ('UNDER_MEM', '암기력', 'UNDER'),
       ('UNDER_DEEPREAD', '책 정독', 'UNDER'),
       ('UNDER_WRITING', '글쓰기', 'UNDER'),
       ('UNDER_LOGIC', '사고력', 'UNDER');

-- 출결 코드 테이블
INSERT INTO attendance_code (attendance_code)
VALUES ('수업 전'),
       ('출석 완료'),
       ('지각'),
       ('결석');

--------------------------------------------------------------------------------------------------------------------------------------------
-- 센터 테이블
INSERT INTO erp_center
    (center_code, center_name, opened_at, biz_no, director_name, tel, center_email, region_key)
VALUES ('PUS001', '본사', '2025-06-25', '0000000000', '임무화', '07052226240', 'st8898ds@gmail.com', 'PUS'),
       ('DAE001', '월성점', '2025-06-25', '0000000000', '황아름', '0532628898', 'st8898ds@gmail.com', 'DAE');

-- 유저 테이블
INSERT INTO erp_user (user_code, user_id, user_name, password_hash, role_key, center_code, created_at)
VALUES ('PUS001ssar', 'ssar', '김하나', '1234', 'ADMIN', 'PUS001', NOW()),
       ('DAE001cos', 'cos', '이하나', '1234', 'MANAGER', 'DAE001', NOW()),
       ('DAE001mango', 'mango', '박하나', '1234', 'TEACHER', 'DAE001', NOW()),
       ('DAE001love', 'love', '최하나', '1234', 'TEACHER', 'DAE001', NOW()),
       ('PUS001haha', 'haha', '정하나', '1234', 'MANAGER', 'PUS001', NOW());

INSERT INTO erp_student
(student_id, student_name, birth, gender, school, address, address_detail,
 entry_han_date, entry_book_date, created_at, grade_key, level_key, status_key,
 center_code, app_id, student_privacy_agree)
VALUES ('abc001', '김호호', '2018-08-15', 1, '호호유치원', '부산광역시 해운대구 센텀중앙로97', '센텀스카이비즈 A동 2810호',
        '2025-06-11', '2025-06-12', NOW(), '05', 'L6', 'ACTIVE', 'DAE001', '629548860', 1),

       ('abc002', '김호일', '2017-07-15', 1, '호호초등학교', '부산광역시 해운대구 센텀중앙로96', '센텀스카이비즈 A동 2809호',
        '2025-06-12', '2025-06-11', NOW(), '06', 'L7', 'ACTIVE', 'DAE001', '000000001', 1),

       ('abc003', '김호이', '2016-06-15', 0, '호호초등학교', '부산광역시 해운대구 센텀중앙로95', '센텀스카이비즈 A동 2808호',
        '2025-06-11', '2025-06-12', NOW(), '07', 'L5', 'ACTIVE', 'DAE001', '000000002', 1),

       ('abc004', '김호삼', '2018-08-15', 1, '호호유치원', '부산광역시 해운대구 센텀중앙로97', '센텀스카이비즈 A동 2810호',
        '2025-06-10', '2025-06-11', NOW(), '11', 'L6', 'ACTIVE', 'DAE001', '000000003', 1),

       ('abc005', '김호사', '2017-07-15', 1, '호호초등학교', '부산광역시 해운대구 센텀중앙로96', '센텀스카이비즈 A동 2809호',
        '2025-06-09', '2025-06-12', NOW(), '12', 'L7', 'ACTIVE', 'DAE001', '000000004', 1),

       ('abc006', '김호오', '2016-06-15', 0, '호호초등학교', '부산광역시 해운대구 센텀중앙로95', '센텀스카이비즈 A동 2808호',
        '2025-06-10', '2025-06-13', NOW(), '13', 'L5', 'ACTIVE', 'DAE001', '000000005', 1),

       ('abc007', '김호육', '2018-08-15', 1, '호호유치원', '부산광역시 해운대구 센텀중앙로97', '센텀스카이비즈 A동 2810호',
        '2025-06-15', '2025-06-14', NOW(), '05', 'L6', 'ACTIVE', 'DAE001', '000000006', 1),

       ('abc008', '김호칠', '2017-07-15', 1, '호호초등학교', '부산광역시 해운대구 센텀중앙로96', '센텀스카이비즈 A동 2809호',
        '2025-06-15', '2025-06-16', NOW(), '06', 'L7', 'ACTIVE', 'DAE001', '000000007', 1),

       ('abc009', '김호팔', '2016-06-15', 0, '호호초등학교', '부산광역시 해운대구 센텀중앙로95', '센텀스카이비즈 A동 2808호',
        '2025-06-10', '2025-06-11', NOW(), '07', 'L5', 'ACTIVE', 'DAE001', '000000008', 1),

       ('abc010', '김호구', '2018-08-15', 1, '호호유치원', '부산광역시 해운대구 센텀중앙로97', '센텀스카이비즈 A동 2810호',
        '2025-06-11', '2025-06-12', NOW(), '11', 'L6', 'ACTIVE', 'DAE001', '000000009', 1),

       ('abc011', '김호열', '2017-07-15', 1, '호호초등학교', '부산광역시 해운대구 센텀중앙로96', '센텀스카이비즈 A동 2809호',
        '2025-06-11', '2025-06-11', NOW(), '12', 'L7', 'ACTIVE', 'DAE001', '000000010', 1),

       ('abc012', '김호가', '2016-06-15', 0, '호호초등학교', '부산광역시 해운대구 센텀중앙로95', '센텀스카이비즈 A동 2808호',
        '2025-06-11', '2025-06-10', NOW(), '13', 'L5', 'ACTIVE', 'DAE001', '000000011', 1),

       ('abc013', '김호나', '2018-08-15', 1, '호호유치원', '부산광역시 해운대구 센텀중앙로97', '센텀스카이비즈 A동 2810호',
        '2025-06-12', '2025-06-12', NOW(), '14', 'L6', 'ACTIVE', 'DAE001', '000000012', 1),

       ('abc014', '김호다', '2017-07-15', 1, '호호초등학교', '부산광역시 해운대구 센텀중앙로96', '센텀스카이비즈 A동 2809호',
        '2025-06-11', '2025-06-11', NOW(), '15', 'L7', 'ACTIVE', 'DAE001', '000000013', 1),

       ('abc015', '김호라', '2016-06-15', 0, '호호초등학교', '부산광역시 해운대구 센텀중앙로95', '센텀스카이비즈 A동 2808호',
        '2025-06-10', '2025-06-10', NOW(), '15', 'L5', 'ACTIVE', 'DAE001', '000000014', 1),

       ('abc016', '김호마', '2016-06-15', 0, '호호초등학교', '부산광역시 해운대구 센텀중앙로95', '센텀스카이비즈 A동 2808호',
        NULL, NULL, NOW(), '06', 'L6', 'ACTIVE', 'DAE001', '000000015', 1);;

INSERT INTO class_instance(class_no, unit_no, user_no)
VALUES (1, 1, 2),
       (8, 1, 3),
       (2, 1, 2),
       (9, 1, 3),
       (3, 1, 2),
       (10, 1, 2),
       (4, 1, 2),
       (11, 1, 2),
       (1, 1, 2),
       (8, 1, 3),
       (2, 1, 2),
       (9, 1, 3),
       (3, 1, 2),
       (10, 1, 2),
       (4, 1, 2),
       (11, 1, 2),
       (1, 1, 2),
       (8, 1, 3),
       (2, 1, 2),
       (9, 1, 3),
       (3, 1, 2),
       (10, 1, 2),
       (4, 1, 2),
       (11, 1, 2);

INSERT INTO student_class (student_no, class_instance_no)
VALUES (1, 1),
       (1, 2),
       (2, 3),
       (2, 4),
       (3, 5),
       (3, 6),
       (4, 7),
       (4, 8),
       (5, 9),
       (5, 10),
       (6, 11),
       (6, 12),
       (7, 13),
       (7, 14),
       (8, 14),
       (8, 16),
       (9, 5),
       (9, 6),
       (10, 1),
       (10, 2),
       (11, 3),
       (11, 4),
       (12, 5),
       (12, 6);

INSERT INTO student_transfer_history (student_no, from_user_no, to_user_no, class_type, transfer_reason, move_at, created_at)
VALUES (1, 2, 3, 1, '인원 미달', '2025-07-06', now()),
       (1, 3, 2, 1, '인원 미달', '2025-07-06', now()),
       (1, 2, 3, 1, '인원 미달', '2025-07-06', now()),
       (1, 3, 2, 1, '인원 미달', '2025-07-06', now()),
       (1, 2, 3, 1, '인원 미달', '2025-07-06', now()),
       (2, 2, 3, 1, '인원 미달', '2025-07-06', now());


INSERT INTO erp_status_history (student_id, status_key, user_id, reason, updated_At)
VALUES (1, 'ACTIVE', 2, NULL, NOW()),
       (2, 'ACTIVE', 2, NULL, NOW()),
       (3, 'PAUSED', 2, '여행으로 인한 1개월 휴원', NOW()),
       (4, 'ACTIVE', 2, NULL, NOW()),
       (5, 'ACTIVE', 2, NULL, NOW()),
       (6, 'ACTIVE', 2, NULL, NOW()),
       (7, 'ACTIVE', 2, NULL, NOW()),
       (8, 'ACTIVE', 2, NULL, NOW()),
       (9, 'ACTIVE', 2, NULL, NOW()),
       (10, 'ACTIVE', 2, NULL, NOW()),
       (11, 'ACTIVE', 2, NULL, NOW()),
       (12, 'ACTIVE', 2, NULL, NOW()),
       (13, 'ACTIVE', 2, NULL, NOW()),
       (14, 'ACTIVE', 2, NULL, NOW()),
       (15, 'ACTIVE', 2, NULL, NOW()),
       (16, 'ACTIVE', 2, NULL, NOW());

INSERT INTO sibling(sibling_code, student_no)
VALUES ('20250001', 1),
       ('20250001', 2);

INSERT INTO erp_time_table (yy, mm, dayname, period_no, start_time, end_time, class_key, unit_no, grade_key, user_code, created_at, time_table_key)
VALUES ('2025', '09', 'mon', 1, '07:50', '08:50', 'C001', 'H01', '13', 'DAE001cos', NOW(), '820cc0f0-001e-4919-9863-e7ffb30b57b8'),
       ('2025', '09', 'wed', 2, '13:50', '18:11', 'C009', 'H01', '13', 'DAE001cos', NOW(), '8349af2f-b797-4d02-9eaf-6e88f2cd7975'),
       ('2025', '09', 'fri', 5, '12:50', '14:00', 'C001', 'H02', '13', 'DAE001cos', NOW(), '8b955091-3f2c-430b-b7e1-565ffa2ee418'),
       ('2025', '09', 'wed', 3, '14:50', '15:40', 'C002', 'H02', '14', 'DAE001cos', NOW(), '7d116444-70a2-4670-bc19-1188d5f8fea7'),
       ('2025', '09', 'thu', 1, '12:00', '13:00', 'C007', 'H04', '06', 'DAE001cos', NOW(), '94fbba5d-20cc-4f75-9390-34b99b583240'),
       ('2025', '09', 'mon', 2, '13:00', '14:00', 'C0013', 'H04', '06', 'DAE001cos', NOW(), '55daf811-622d-4641-a686-dece5d2ef8c1'),
       ('2025', '09', 'mon', 3, '14:00', '15:00', 'C003', 'H04', '11', 'DAE001cos', NOW(), '5060541a-3e77-464c-9172-46ed0795370c'),
       ('2025', '09', 'mon', 4, '15:00', '16:00', 'C009', 'H04', '11', 'DAE001cos', NOW(), '00f58d5f-8a72-45f4-b855-6956ace38b01'),
       ('2025', '09', 'thu', 6, '17:00', '18:00', 'C006', 'H01', '15', 'DAE001cos', NOW(), '8587aa05-5311-4653-a46f-5e4d3a527ed0'),
       ('2025', '09', 'tue', 1, '12:00', '13:00', 'C004', 'H03', '13', 'DAE001cos', NOW(), 'f768299f-b100-4bb5-804d-1731b1b12fb0'),
       ('2025', '09', 'tue', 2, '13:00', '14:00', 'C008', 'H05', '13', 'DAE001cos', NOW(), '4599a769-15d3-4b90-a3ad-faef590b448d'),
       ('2025', '09', 'tue', 5, '16:00', '17:00', 'C004', 'H04', '11', 'DAE001cos', NOW(), '170273f4-8666-468f-a634-d1989d59970a'),
       ('2025', '09', 'tue', 6, '17:00', '18:00', 'C0011', 'H02', '11', 'DAE001cos', NOW(), 'a43c5e45-0365-4184-b367-845d6a3ffd12'),
       ('2025', '09', 'sat', 6, '07:50', '08:50', 'C003', 'H03', '12', 'DAE001mango', NOW(), '211ffdb2-8c5a-4bdd-aeed-abcbe5651849');;

INSERT INTO erp_time_table_assign (student_no, week, time_table_code)
VALUES (1, '4', '820cc0f0-001e-4919-9863-e7ffb30b57b8'),
       (2, '3', '820cc0f0-001e-4919-9863-e7ffb30b57b8'),
       (3, '2', '820cc0f0-001e-4919-9863-e7ffb30b57b8'),
       (4, '1', '820cc0f0-001e-4919-9863-e7ffb30b57b8'),
       (1, '4', '55daf811-622d-4641-a686-dece5d2ef8c1'),
       (2, '4', '55daf811-622d-4641-a686-dece5d2ef8c1'),
       (3, '4', '55daf811-622d-4641-a686-dece5d2ef8c1'),
       (4, '3', '55daf811-622d-4641-a686-dece5d2ef8c1'),
       (12, '4', '8349af2f-b797-4d02-9eaf-6e88f2cd7975'),
       (13, '4', '8349af2f-b797-4d02-9eaf-6e88f2cd7975');

INSERT INTO erp_time_table_code (time_table_key, time_table_label, time_table_ym)
VALUES ('820cc0f0-001e-4919-9863-e7ffb30b57b8', '월 12:50 ~ 13:40 초등천재 1호', '202509'),
       ('8349af2f-b797-4d02-9eaf-6e88f2cd7975', '수 13:50 ~ 14:40 다움 1호', '202509'),
       ('8b955091-3f2c-430b-b7e1-565ffa2ee418', '금 12:50 ~ 13:40 초등천재 2호', '202509'),
       ('7d116444-70a2-4670-bc19-1188d5f8fea7', '수 14:50 ~ 15:40 초등수재 2호', '202509'),
       ('94fbba5d-20cc-4f75-9390-34b99b583240', '목 12:00 ~ 13:00 호호하니 4호', '202509'),
       ('55daf811-622d-4641-a686-dece5d2ef8c1', '월 13:00 ~ 14:00 호호부키 4호', '202509'),
       ('5060541a-3e77-464c-9172-46ed0795370c', '월 14:00 ~ 15:00 초등박사 4호', '202509'),
       ('00f58d5f-8a72-45f4-b855-6956ace38b01', '월 15:00 ~ 16:00 다움 4호', '202509'),
       ('8587aa05-5311-4653-a46f-5e4d3a527ed0', '목 17:00 ~ 18:00 한스쿨II 1호', '202509'),
       ('f768299f-b100-4bb5-804d-1731b1b12fb0', '화 12:00 ~ 13:00 초등신동 3호', '202509'),
       ('4599a769-15d3-4b90-a3ad-faef590b448d', '화 13:00 ~ 14:00 새움 5호', '202509'),
       ('170273f4-8666-468f-a634-d1989d59970a', '화 16:00 ~ 17:00 초등신동 4호', '202509'),
       ('a43c5e45-0365-4184-b367-845d6a3ffd12', '화 17:00 ~ 18:00 채움 2호', '202509'),
       ('211ffdb2-8c5a-4bdd-aeed-abcbe5651849', '토 07:50 ~ 08:50 초등박사 3호', '202509');

INSERT INTO student_monthly_snapshot (snapshot_ym, active_count, rest_count, withdrawn_count, wait_count, created_at)
VALUES ('2024-07', 15, 0, 0, 0, now()),
       ('2024-08', 16, 0, 0, 0, now()),
       ('2024-09', 15, 0, 0, 0, now()),
       ('2024-10', 16, 0, 0, 0, now()),
       ('2024-11', 15, 0, 0, 0, now()),
       ('2024-12', 16, 0, 0, 0, now()),
       ('2025-01', 10, 0, 0, 0, now()),
       ('2025-02', 12, 0, 0, 0, now()),
       ('2025-03', 16, 0, 0, 0, now()),
       ('2025-04', 17, 0, 0, 0, now()),
       ('2025-05', 13, 0, 0, 0, now()),
       ('2025-06', 12, 0, 0, 0, now()),
       ('2025-07', 20, 0, 0, 0, now());

INSERT INTO consult (student_name, consult_date, school, grade_no, phone, inflow_route_no, content, created_at)
VALUES ('김호일', '2025-07-11', '호호부설유치원', 1, '010-1234-5678', 1, '내용내용내용내용', now()),
       ('김호이', '2025-07-12', '호호부설유치원', 2, '010-1234-5678', 2, '내용내용내용내용', now()),
       ('김호삼', '2025-07-13', '호호부설유치원', 3, '010-1234-5678', 3, '내용내용내용내용', now()),
       ('김호사', '2025-07-14', '호호부설유치원', 4, '010-1234-5678', 4, '내용내용내용내용', now()),
       ('김호오', '2025-07-15', '호호부설유치원', 5, '010-1234-5678', 1, '내용내용내용내용', now()),
       ('김호육', '2025-07-16', '호호부설유치원', 6, '010-1234-5678', 2, '내용내용내용내용', now()),
       ('김호칠', '2025-07-17', '호호부설유치원', 7, '010-1234-5678', 3, '내용내용내용내용', now()),
       ('김호팔', '2025-07-18', '호호부설유치원', 8, '010-1234-5678', 4, '내용내용내용내용', now());

INSERT INTO student_remark (ym, week, student_no, remark_code_no)
VALUES ('202509', 2, 1, 1),
       ('202509', 2, 1, 2),
       ('202509', 2, 1, 5),
       ('202509', 2, 2, 1),
       ('202509', 2, 2, 4),
       ('202509', 2, 2, 6);

-- INSERT INTO remedial(remedial_subject, absence_date, remedial_date, student_no, user_no, action, updated_at, time_table_no)
-- VALUES ('초등천재 2호', '2025-07-10', '2025-07-20', 1, 2, false, now(), 1),
--        ('초등천재 3호', '2025-08-10', '2025-08-15', 1, 2, false, now(), 1),
--        ('초등신동 3호', '2025-08-10', null, 1, 2, false, now(), 1),
--        ('초등수재 3호', '2025-08-10', '2025-08-15', 2, 2, true, now(), 1),
--        ('초등박사 3호', '2025-08-10', '2025-08-15', 3, 2, false, now(), 1);

INSERT INTO student_attendance (student_no, attendance_no, created_at, in_time, attendance_date, center_code)
VALUES (1, 1, NOW(), '07:45', '2025-09-02', 'DAE001'),
       (2, 2, NOW(), '08:01', '2025-09-02', 'DAE001');

INSERT INTO monthly_score
(question1, question2, question3, question4, question5, question6, question7, question8, student_no, yy, mm, time_table_code, created_at)
VALUES (true, false, true, true, false, true, false, true, 1, '2025', '09', '820cc0f0-001e-4919-9863-e7ffb30b57b8', NOW()),
       (false, false, true, false, true, false, true, true, 2, '2025', '09', '820cc0f0-001e-4919-9863-e7ffb30b57b8', NOW()),
       (false, false, false, false, false, false, false, false, 3, '2025', '09', '820cc0f0-001e-4919-9863-e7ffb30b57b8', NOW()),
       (false, false, false, false, false, false, false, false, 4, '2025', '09', '820cc0f0-001e-4919-9863-e7ffb30b57b8', NOW()),
       (false, false, false, false, false, false, false, false, 1, '2025', '09', '55daf811-622d-4641-a686-dece5d2ef8c1', NOW()),
       (false, false, false, false, false, false, false, false, 2, '2025', '09', '55daf811-622d-4641-a686-dece5d2ef8c1', NOW()),
       (false, false, false, false, false, false, false, false, 3, '2025', '09', '55daf811-622d-4641-a686-dece5d2ef8c1', NOW()),
       (false, false, false, false, false, false, false, false, 4, '2025', '09', '55daf811-622d-4641-a686-dece5d2ef8c1', NOW()),
       (true, true, true, true, true, true, true, true, 12, '2025', '09', '8349af2f-b797-4d02-9eaf-6e88f2cd7975', NOW()),
       (true, false, true, false, true, false, true, false, 13, '2025', '09', '8349af2f-b797-4d02-9eaf-6e88f2cd7975', NOW());