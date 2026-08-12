-- 복구: 3번 재정렬 스크립트가 ROW_NUMBER()를 써서 같은 round(같은 배치)의 행들을
-- 서로 다른 번호로 흩어놓은 걸 되돌린다.
-- created_at은 한 번도 건드리지 않았으므로, "같은 배치 = created_at이 완전히 동일한 행들"
-- 기준으로 DENSE_RANK() 다시 매기면 원래 묶음으로 복구되고, 동시에 빈틈없는 번호도 그대로 나온다.

;WITH FixRound AS (
    SELECT id,
           DENSE_RANK() OVER (
               PARTITION BY user_code, yy, mm
               ORDER BY created_at
               ) AS correct_round
    FROM erp_order_history
    WHERE yy = '2026' AND mm IN ('08', '09')
)
UPDATE h
SET h.round = f.correct_round
FROM erp_order_history h
         JOIN FixRound f ON h.id = f.id
WHERE h.round <> f.correct_round;
