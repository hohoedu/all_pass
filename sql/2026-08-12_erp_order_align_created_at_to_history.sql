-- erp_order.created_at을 같은 배치의 erp_order_history 마지막 회차 created_at에 맞춰 정렬
-- (insertOrder와 insertOrderHistory가 같은 흐름에서 순차적으로 각각 SYSDATETIME()을 호출하다보니
--  같은 배치인데도 밀리초 단위로 시각이 어긋나 있음 — history 쪽 시각을 기준으로 통일)

-- ── 1) 미리보기: 지금 어긋나 있는 선생님/월과, 맞출 시각 ──
;WITH LastRound AS (
    SELECT user_code, yy, mm, MAX(round) AS last_round
    FROM erp_order_history
    WHERE yy = '2026' AND mm IN ('08', '09')
    GROUP BY user_code, yy, mm
),
LastRoundDate AS (
    SELECT lr.user_code, lr.yy, lr.mm, MAX(h.created_at) AS history_created_at
    FROM erp_order_history h
             JOIN LastRound lr
                  ON h.user_code = lr.user_code AND h.yy = lr.yy AND h.mm = lr.mm AND h.round = lr.last_round
    GROUP BY lr.user_code, lr.yy, lr.mm
)
SELECT o.user_code,
       o.yy,
       o.mm,
       CONVERT(VARCHAR(23), o.created_at, 121)         AS current_order_created_at,
       CONVERT(VARCHAR(23), lrd.history_created_at, 121) AS history_created_at
FROM erp_order o
         JOIN LastRoundDate lrd
              ON o.user_code = lrd.user_code AND o.yy = lrd.yy AND o.mm = lrd.mm
WHERE o.created_at <> lrd.history_created_at;


-- ── 2) 미리보기 결과가 맞으면 아래 UPDATE 블록의 주석을 풀고 실행 ──
;WITH LastRound AS (
   SELECT user_code, yy, mm, MAX(round) AS last_round
   FROM erp_order_history
   WHERE yy = '2026' AND mm IN ('08', '09')
   GROUP BY user_code, yy, mm
),
LastRoundDate AS (
   SELECT lr.user_code, lr.yy, lr.mm, MAX(h.created_at) AS history_created_at
   FROM erp_order_history h
            JOIN LastRound lr
                 ON h.user_code = lr.user_code AND h.yy = lr.yy AND h.mm = lr.mm AND h.round = lr.last_round
   GROUP BY lr.user_code, lr.yy, lr.mm
)
UPDATE o
SET o.created_at = lrd.history_created_at
FROM erp_order o
        JOIN LastRoundDate lrd
             ON o.user_code = lrd.user_code AND o.yy = lrd.yy AND o.mm = lrd.mm
WHERE o.created_at <> lrd.history_created_at;
