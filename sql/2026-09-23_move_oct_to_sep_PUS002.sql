/* ============================================================
   [이우현 / PUS002] 10월로 잘못 청구·결제된 건을 9월로 이관
   ------------------------------------------------------------
   9월 payment : PUS002260807164629CCK  (issued, 미납 180,000)
   10월 payment: PUS002260903135059BVD  (approved, 9/23 결제완료)
   이관 대상   : bill id 8545(EDU 150,000), 8853(BOOK 30,000)
                 callback id 7084, 7085
   실행일      : 2026-09-23
   ============================================================ */

BEGIN TRAN;

/* ── 1. 청구서(bill) 2건을 9월 payment 로 이관 ────────────── */
UPDATE erp_payment_bill
SET    payment_key = 'PUS002260807164629CCK',
       mm          = '09',
       updated_at  = SYSDATETIME()
WHERE  id IN (8545, 8853);


/* ── 2. 승인 콜백 2건의 귀속월 변경 ──────────────────────
   appr_date(20260923…)는 실제 승인일이므로 그대로 둡니다.
   장부/매출대조가 appr_date 기준이라 바꾸면 대조가 깨집니다. */
UPDATE erp_payment_callback
SET    mm = '09'
WHERE  id IN (7084, 7085);


/* ── 3. 9월 payment → 결제완료 ───────────────────────── */
UPDATE erp_payment
SET    status        = 'approved',
       paid_date     = '2026-09-23 12:37',
       method        = 'paymint',
       unpaid_amount = 0,
       updated_at    = SYSDATETIME()
WHERE  payment_key = 'PUS002260807164629CCK';


/* ── 4. 10월 payment → 미청구 상태로 초기화 (재청구 가능하게) ── */
UPDATE erp_payment
SET    status        = 'pending',
       paid_date     = NULL,
       method        = NULL,
       unpaid_amount = 180000,
       updated_at    = SYSDATETIME()
WHERE  payment_key = 'PUS002260903135059BVD';


/* ── 5. 이력 ─────────────────────────────────────── */
INSERT INTO erp_payment_history
       (event_type, event_source, old_status, new_status, amount, description, payment_key, created_at)
VALUES ('month_moved','admin','issued','approved',180000,
        '10월 오청구·결제분(bill 8545,8853)을 9월 귀속으로 이관',
        'PUS002260807164629CCK', SYSDATETIME()),
       ('month_moved','admin','approved','pending',180000,
        '10월 결제건을 9월로 이관, 10월 재청구 위해 초기화',
        'PUS002260903135059BVD', SYSDATETIME());


/* ── 6. 검증 (COMMIT 전에 결과 확인) ─────────────────── */
SELECT p.payment_key, p.yy, p.mm, p.status, p.amount, p.unpaid_amount, p.paid_date, p.method
FROM   erp_payment p
WHERE  p.payment_key IN ('PUS002260807164629CCK','PUS002260903135059BVD');
-- 기대값: 09 → approved / 180000 / 0 / 2026-09-23
--         10 → pending  / 180000 / 180000 / NULL

SELECT b.id, b.bill_id, b.payment_key, b.bill_type, b.status, b.amount, b.yy, b.mm
FROM   erp_payment_bill b
WHERE  b.id IN (8545, 8853);
-- 기대값: 둘 다 payment_key = ...CCK, mm = '09', status = approved

SELECT c.id, c.bill_id, c.appr_num, c.appr_date, c.yy, c.mm
FROM   erp_payment_callback c
WHERE  c.id IN (7084, 7085);
-- 기대값: mm = '09', appr_date 는 20260923… 유지

-- 위 3개 결과가 기대값과 같으면
-- COMMIT TRAN;
-- 다르면
-- ROLLBACK TRAN;
