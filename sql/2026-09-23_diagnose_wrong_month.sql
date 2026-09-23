/* ============================================================
   [진단용] 10월로 잘못 청구/결제된 건 파악
   - 아래 3개 변수만 채우고 전체 실행 → 결과 7개를 그대로 전달
   - 조회(SELECT)만 있으므로 데이터 변경 없음
   ============================================================ */

DECLARE @centerCode VARCHAR(20) = 'XXX001';   -- 학원 코드
DECLARE @yy         VARCHAR(4)  = '2026';
DECLARE @names      VARCHAR(500) = '홍길동,김철수';  -- 대상 학생 이름 콤마 구분 (전체면 '' 로 두기)

DECLARE @targets TABLE (student_id VARCHAR(50));
INSERT INTO @targets
SELECT s.student_id
FROM   erp_student s
WHERE  s.center_code = @centerCode
  AND  (@names = '' OR s.student_name IN (SELECT LTRIM(RTRIM(value)) FROM STRING_SPLIT(@names, ',')));


/* ── ① 9월/10월 payment 마스터 현황 ───────────────── */
SELECT '1_payment' AS q, s.student_name, p.payment_key, p.yy, p.mm,
       p.status, p.amount, p.unpaid_amount, p.paid_date, p.method
FROM   erp_payment p
JOIN   erp_student s ON s.student_id = p.student_id
WHERE  p.student_id IN (SELECT student_id FROM @targets)
  AND  p.yy = @yy AND p.mm IN ('09','10')
ORDER  BY s.student_name, p.mm;


/* ── ② detail(청구 항목) 금액 - 9월 vs 10월 비교 ──── */
SELECT '2_detail' AS q, s.student_name, p.mm, pd.item_type,
       SUM(pd.amount) AS detail_amount, COUNT(*) AS cnt
FROM   erp_payment_detail pd
JOIN   erp_payment p  ON p.payment_key = pd.payment_key
JOIN   erp_student s  ON s.student_id  = p.student_id
WHERE  p.student_id IN (SELECT student_id FROM @targets)
  AND  p.yy = @yy AND p.mm IN ('09','10')
GROUP  BY s.student_name, p.mm, pd.item_type
ORDER  BY s.student_name, p.mm, pd.item_type;


/* ── ③ 발행된 청구서(bill) ─────────────────────────
   같은 bill_id 에 다른 학생(형제)이 묶여 있는지 확인용으로
   sibling_cnt 를 함께 조회                               */
SELECT '3_bill' AS q, s.student_name, b.id, b.bill_id, b.payment_key,
       b.bill_type, b.status, b.amount, b.yy, b.mm,
       b.issued_date, b.expire_date,
       (SELECT COUNT(*) FROM erp_payment_bill b2 WHERE b2.bill_id = b.bill_id) AS sibling_cnt
FROM   erp_payment_bill b
JOIN   erp_payment p  ON p.payment_key = b.payment_key
JOIN   erp_student s  ON s.student_id  = p.student_id
WHERE  p.student_id IN (SELECT student_id FROM @targets)
  AND  b.yy = @yy AND b.mm IN ('09','10')
ORDER  BY s.student_name, b.mm, b.bill_type;


/* ── ④ 결제 승인 콜백 (카카오페이/카드 결제분) ────── */
SELECT '4_callback' AS q, s.student_name, c.id, c.bill_id, c.appr_num,
       c.appr_date, c.appr_price, c.appr_state, c.appr_pay_type,
       c.appr_issuer, c.yy, c.mm
FROM   erp_payment_callback c
JOIN   erp_payment_bill b ON b.bill_id = c.bill_id
JOIN   erp_payment p      ON p.payment_key = b.payment_key
JOIN   erp_student s      ON s.student_id  = p.student_id
WHERE  p.student_id IN (SELECT student_id FROM @targets)
  AND  c.yy = @yy AND c.mm IN ('09','10')
ORDER  BY s.student_name, c.appr_date;


/* ── ⑤ 현장결제 / 선결제 (manual) ─────────────────── */
SELECT '5_manual' AS q, s.student_name, m.id, m.manual_key, m.payment_key,
       m.card_amount, m.cash_amount, m.transfer_amount, m.card_name,
       m.paid_date, m.yy, m.mm, g.source, g.method, g.total_amount
FROM   erp_payment_manual m
LEFT   JOIN erp_payment_manual_group g ON g.manual_key = m.manual_key
JOIN   erp_student s ON s.student_id = m.student_id
WHERE  m.student_id IN (SELECT student_id FROM @targets)
  AND  m.yy = @yy AND m.mm IN ('09','10')
ORDER  BY s.student_name, m.mm;


/* ── ⑥ 현금영수증 발행 여부 ───────────────────────── */
SELECT '6_cashbill' AS q, s.student_name, cb.id, cb.bill_id, cb.payment_key,
       cb.price, cb.status, cb.issue_date, cb.appr_cash_num
FROM   erp_payment_cashbill cb
JOIN   erp_student s ON s.student_id = cb.student_id
WHERE  cb.student_id IN (SELECT student_id FROM @targets)
  AND  cb.issue_date >= @yy + '-09-01'
ORDER  BY s.student_name, cb.issue_date;


/* ── ⑦ 9월/10월 시간표 등록 현황 (payment 행 존재 근거) ── */
SELECT '7_timetable' AS q, s.student_name, tt.time_table_key, tt.yy, tt.mm,
       tt.class_key, tt.dayname
FROM   erp_time_table_assign ta
JOIN   erp_time_table tt ON tt.time_table_key = ta.time_table_key
JOIN   erp_student s     ON s.student_id = ta.student_id
WHERE  ta.student_id IN (SELECT student_id FROM @targets)
  AND  tt.yy = @yy AND tt.mm IN ('09','10')
ORDER  BY s.student_name, tt.mm;
