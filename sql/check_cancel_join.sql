DECLARE @studentId NVARCHAR(50) = 'PUS002260708637B7';

WITH ta AS (SELECT * FROM erp_teacher_assign WHERE student_id = @studentId),
     sub AS (SELECT '1'                                AS ct,
                    TRY_CAST(entry_han_date AS DATE)   AS entryDate,
                    assign_han_teacher                 AS tch,
                    inactive_han_date                  AS inact,
                    CASE WHEN entry_book_date IS NULL AND inactive_book_date IS NULL
                             THEN 1 ELSE 0 END         AS promotes
             FROM ta
             UNION ALL
             SELECT '2',
                    TRY_CAST(entry_book_date AS DATE),
                    assign_book_teacher,
                    inactive_book_date,
                    CASE WHEN entry_han_date IS NULL AND inactive_han_date IS NULL
                             THEN 1 ELSE 0 END
             FROM ta),
     j AS (SELECT s.*,
                  CASE
                      WHEN s.entryDate IS NULL THEN '취소불가 - 입회내역 없음'
                      WHEN s.inact IS NOT NULL THEN '취소불가 - 탈퇴 이력'
                      WHEN EXISTS (SELECT 1 FROM erp_payment_bill
                                   WHERE student_id = @studentId AND status = 'approved')
                          THEN '취소불가 - 승인 결제'
                      WHEN EXISTS (SELECT 1 FROM erp_before_class_notice
                                   WHERE student_id = @studentId AND class_type = s.ct
                                     AND TRY_CAST(class_date AS DATE) > DATEADD(DAY, 7, s.entryDate))
                          THEN '취소불가 - 첫 주 이후 알림장'
                      WHEN EXISTS (SELECT 1
                                   FROM erp_student_attendance sa
                                            JOIN erp_time_table tt ON tt.time_table_key = sa.time_table_key
                                            JOIN erp_class_code cc ON cc.class_key = tt.class_key
                                   WHERE sa.student_id = @studentId AND cc.class_type = s.ct
                                     AND TRY_CAST(sa.attendance_date AS DATE) > DATEADD(DAY, 7, s.entryDate))
                          THEN '취소불가 - 첫 주 이후 출결'
                      ELSE '취소 가능'
                      END AS verdict
           FROM sub s)

SELECT * FROM (
    SELECT 1 AS ord, '학생' AS 구분, '이름' AS 항목,
           CAST(MAX(student_name) AS NVARCHAR(100)) AS 값
    FROM erp_student WHERE student_id = @studentId
    UNION ALL
    SELECT 2, '학생', '센터/학년', CAST(MAX(center_code) + ' / ' + MAX(grade_key) AS NVARCHAR(100))
    FROM erp_student WHERE student_id = @studentId
    UNION ALL
    SELECT 3, '결제', '승인건수',
           CAST((SELECT COUNT(*) FROM erp_payment_bill
                 WHERE student_id = @studentId AND status = 'approved') AS NVARCHAR(100))
    UNION ALL
    SELECT 4, '탈퇴로그', '건수',
           CAST((SELECT COUNT(*) FROM erp_student_withdraw_log
                 WHERE student_id = @studentId) AS NVARCHAR(100))

    UNION ALL
    SELECT 10 + CAST(ct AS INT), CASE ct WHEN '1' THEN '한스쿨' ELSE '북스쿨' END, '입회일',
           ISNULL(CONVERT(NVARCHAR(10), entryDate, 23), '(없음)') FROM j
    UNION ALL
    SELECT 20 + CAST(ct AS INT), CASE ct WHEN '1' THEN '한스쿨' ELSE '북스쿨' END, '유예마감(+7일)',
           ISNULL(CONVERT(NVARCHAR(10), DATEADD(DAY, 7, entryDate), 23), '-') FROM j
    UNION ALL
    SELECT 30 + CAST(ct AS INT), CASE ct WHEN '1' THEN '한스쿨' ELSE '북스쿨' END, '담당',
           ISNULL(tch, '(없음)') FROM j
    UNION ALL
    SELECT 40 + CAST(ct AS INT), CASE ct WHEN '1' THEN '한스쿨' ELSE '북스쿨' END, '탈퇴일',
           ISNULL(CAST(inact AS NVARCHAR(30)), '-') FROM j
    UNION ALL
    SELECT 50 + CAST(ct AS INT), CASE ct WHEN '1' THEN '한스쿨' ELSE '북스쿨' END, '>>> 판정',
           verdict FROM j
    UNION ALL
    SELECT 60 + CAST(ct AS INT), CASE ct WHEN '1' THEN '한스쿨' ELSE '북스쿨' END, '취소시 학생삭제',
           CASE promotes WHEN 1 THEN '예 - 완전삭제' ELSE '아니오 - 상대과목 유지' END FROM j

    UNION ALL
    SELECT 70 + CAST(ct AS INT), CASE ct WHEN '1' THEN '한스쿨' ELSE '북스쿨' END, '삭제될 건수',
           '알림장전 ' + CAST((SELECT COUNT(*) FROM erp_before_class_notice
                            WHERE student_id = @studentId AND class_type = j.ct) AS NVARCHAR(10)) +
           ' / 알림장후 ' + CAST((SELECT COUNT(*) FROM erp_after_class_notice
                               WHERE student_id = @studentId AND class_type = j.ct) AS NVARCHAR(10)) +
           ' / 출결 ' + CAST((SELECT COUNT(*) FROM erp_student_attendance sa
                                     JOIN erp_time_table tt ON tt.time_table_key = sa.time_table_key
                                     JOIN erp_class_code cc ON cc.class_key = tt.class_key
                            WHERE sa.student_id = @studentId AND cc.class_type = j.ct) AS NVARCHAR(10)) +
           ' / 시간표 ' + CAST((SELECT COUNT(*) FROM erp_time_table_assign tta
                                     JOIN erp_time_table tt2 ON tt2.time_table_key = tta.time_table_key
                                     JOIN erp_class_code cc2 ON cc2.class_key = tt2.class_key
                             WHERE tta.student_id = @studentId AND cc2.class_type = j.ct) AS NVARCHAR(10)) +
           ' / 결제상세 ' + CAST((SELECT COUNT(*) FROM erp_payment_detail pd
                                       JOIN erp_payment p ON p.payment_key = pd.payment_key
                              WHERE p.student_id = @studentId AND pd.class_type = j.ct) AS NVARCHAR(10))
    FROM j
) x
ORDER BY ord;
