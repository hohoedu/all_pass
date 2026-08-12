-- 2026년 8월, 9월 erp_order_history 정리
-- 같은 선생님(user_code) 같은 달 안에서, 어떤 회차(round)가 바로 이전 회차와
-- (class_key, unit_key, count) 조합이 완전히 동일하다면 = 실제로는 아무 변동이 없었던 회차이므로 삭제 대상.
-- (round 컬럼을 추가하기 전에는 변동 여부를 걸러내지 않고 매번 쌓았기 때문에 생긴 중복)

-- ── 1) 먼저 삭제될 대상만 미리보기 (실제 삭제 아님) ──
;WITH RoundKeys AS (
    SELECT DISTINCT user_code, yy, mm, round
    FROM erp_order_history
    WHERE yy = '2026' AND mm IN ('08', '09')
),
RoundSignature AS (
    SELECT rk.user_code, rk.yy, rk.mm, rk.round,
           STUFF((
               SELECT '|' + h2.class_key + ':' + h2.unit_key + ':' + CAST(h2.count AS VARCHAR(10))
               FROM erp_order_history h2
               WHERE h2.user_code = rk.user_code
                 AND h2.yy = rk.yy
                 AND h2.mm = rk.mm
                 AND h2.round = rk.round
               ORDER BY h2.class_key, h2.unit_key
               FOR XML PATH(''), TYPE
           ).value('.', 'NVARCHAR(MAX)'), 1, 1, '') AS sig
    FROM RoundKeys rk
),
RoundCompare AS (
    SELECT *,
           LAG(sig) OVER (PARTITION BY user_code, yy, mm ORDER BY round) AS prev_sig
    FROM RoundSignature
)
SELECT h.*
FROM erp_order_history h
         JOIN RoundCompare c
              ON h.user_code = c.user_code
                  AND h.yy = c.yy
                  AND h.mm = c.mm
                  AND h.round = c.round
WHERE c.sig = c.prev_sig
ORDER BY h.user_code, h.yy, h.mm, h.round;


-- ── 2) 미리보기 결과가 맞으면 아래 DELETE 블록의 주석을 풀고 실행 ──
;WITH RoundKeys AS (
   SELECT DISTINCT user_code, yy, mm, round
   FROM erp_order_history
   WHERE yy = '2026' AND mm IN ('08', '09')
),
RoundSignature AS (
   SELECT rk.user_code, rk.yy, rk.mm, rk.round,
          STUFF((
              SELECT '|' + h2.class_key + ':' + h2.unit_key + ':' + CAST(h2.count AS VARCHAR(10))
              FROM erp_order_history h2
              WHERE h2.user_code = rk.user_code
                AND h2.yy = rk.yy
                AND h2.mm = rk.mm
                AND h2.round = rk.round
              ORDER BY h2.class_key, h2.unit_key
              FOR XML PATH(''), TYPE
          ).value('.', 'NVARCHAR(MAX)'), 1, 1, '') AS sig
   FROM RoundKeys rk
),
RoundCompare AS (
   SELECT *,
          LAG(sig) OVER (PARTITION BY user_code, yy, mm ORDER BY round) AS prev_sig
   FROM RoundSignature
),
ToDelete AS (
   SELECT user_code, yy, mm, round
   FROM RoundCompare
   WHERE sig = prev_sig
)
DELETE h
FROM erp_order_history h
        JOIN ToDelete d
             ON h.user_code = d.user_code
                 AND h.yy = d.yy
                 AND h.mm = d.mm
                 AND h.round = d.round;


-- ── 3) 삭제 후, 남은 회차 번호를 빈틈없이 1,2,3...으로 재정렬 ──
-- (2번 DELETE를 실행하고 난 뒤에 이 블록의 주석을 풀고 실행)
-- 주의: 같은 round(같은 배치)엔 품목별로 여러 행이 있으므로 ROW_NUMBER()를 쓰면
-- 같은 배치 행들이 서로 다른 번호로 흩어진다. 반드시 DENSE_RANK()를 써야 하고,
-- created_at은 한 번도 변경되지 않으므로 이걸 기준으로 묶는 게 가장 안전하다.
;WITH Renumber AS (
   SELECT id,
          DENSE_RANK() OVER (PARTITION BY user_code, yy, mm ORDER BY created_at) AS new_round
   FROM erp_order_history
   WHERE yy = '2026' AND mm IN ('08', '09')
)
UPDATE h
SET h.round = r.new_round
FROM erp_order_history h
        JOIN Renumber r ON h.id = r.id
WHERE h.round <> r.new_round;
