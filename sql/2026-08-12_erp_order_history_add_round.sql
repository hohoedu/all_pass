IF COL_LENGTH('erp_order_history', 'round') IS NULL
    BEGIN
        ALTER TABLE erp_order_history
            ADD round INT NULL;
    END

-- 백필: round 컬럼이 없던 시절 데이터는 배치(회차) 구분값이 원래 없으므로,
-- 같은 선생님(user_code)+같은 달(yy, mm) 안에서 created_at 값이 완전히 동일한(밀리초까지) 행들을
-- 한 배치(insertOrderHistory 한 번의 호출)로 보고 순서대로 1, 2, 3... 을 매긴다.
-- (SYSDATETIME()은 INSERT문 한 번당 한 번만 평가되므로, 같은 배치의 행들은
--  created_at이 밀리초까지 완전히 동일함 — 그래서 별도 시간 단위 절삭 없이 값 자체로 비교)
WITH RoundGroup AS (
    SELECT id,
           DENSE_RANK() OVER (
               PARTITION BY user_code, yy, mm
               ORDER BY created_at
               ) AS calc_round
    FROM erp_order_history
    WHERE round IS NULL
)
UPDATE h
SET h.round = g.calc_round
FROM erp_order_history h
         JOIN RoundGroup g ON h.id = g.id
WHERE h.round IS NULL;
