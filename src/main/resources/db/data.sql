-- 유저 구분 코드 테이블 
INSERT INTO user_role_code(user_role) VALUES ('운영자'), ('관리자'),('선생님');

INSERT INTO region_code (region) 
VALUES ('SEO'), ('PUS'), ('DAE'), ('INC'), ('GWJ'), ('DTJ'), ('ULS'), ('SJG'), ('GGD'),('GND'),('GBD'),('JND'),('JBD'),('CND'),('CBD'),('GWD'),('JJD');

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
INSERT INTO status_code (status) VALUES (001), (002), (003), (004);

-- 수업 코드 테이블 
INSERT INTO class_code (class_name, class_type)
VALUES ('초등천재', '1'), ('초등수재', '1'), ('초등박사','1'), ('초등신동','1'), ('한스쿨I','1'), ('한스쿨II','1'), ('호호하니','1'),
       ('새움', '2'), ('다움', '2'), ('티움', '2'), ('채움', '2'), ('북스쿨', '2'), ('호호부키', '2');


INSERT INTO user_tb(user_id, password, username, user_role_no, center_no, created_at)  
VALUES ('ssar', '1234','쌀', 1, 'PUS001', now()), 
       ('cos', '1234', '코스',2, 'DAE001', now()), 
       ('mango', '1234', '망고',2, 'PUS002', now()); 

INSERT INTO student_tb (student_id, student_name, birth, gender, school, address, address_detail, entry_han_date, entry_book_date, created_at, grade_no, level_no, status_no, center_no, student_privacy_agree)  
VALUES ('abc001','김호호', '2018-08-15', true, '호호유치원','부산광역시 해운대구 센텀중앙로97', '센텀스카이비즈 A동 2810호', '2025-06-12','2025-06-12', now(), 1, 3, 1, 'PUS002', true), 
       ('abc002', '김호일', '2017-07-15', true, '호호초등학교','부산광역시 해운대구 센텀중앙로96', '센텀스카이비즈 A동 2809호', '2025-06-11','2025-06-11', now(), 2, 4, 1, 'DAE001', true), 
       ('abc003', '김호이', '2016-06-15', false, '호호초등학교','부산광역시 해운대구 센텀중앙로95', '센텀스카이비즈 A동 2808호', '2025-06-10','2025-06-10', now(), 3, 5, 1, 'PUS002', true),
       ('abc004', '김호삼', '2016-06-15', false, '호호초등학교','부산광역시 해운대구 센텀중앙로95', '센텀스카이비즈 A동 2808호', NULL,NULL, now(), 2, 2, 1, NULL, true);

INSERT INTO student_class (student_no, class_code_no) 
VALUES (1, 1), 
       (1, 8), 
       (2, 2),
       (2, 9),
       (3, 1);

INSERT INTO status_history (student_no, status_no, reason, created_at)
VALUES (1, 1, NULL, now()),
       (2, 1, NULL, now()),
       (3, 3, '여행으로 인한 1개월 휴원', now()),
       (4, 1, NULL, now());