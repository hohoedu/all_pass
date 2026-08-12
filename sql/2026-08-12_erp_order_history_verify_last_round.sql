-- 검증: 현재 erp_order 내용이 erp_order_history의 "마지막 round" 내용과 일치하는지 확인
-- (일치해야 정상 — erp_order는 매번 delete+insert로 덮어쓰고, 같은 시점에 history round도 같이 남기기 때문)
-- 결과가 0건이면 정상, 결과가 있으면 해당 선생님/월은 어긋난 것

;WITH OrderSig AS (
    SELECT o1.user_code, o1.yy, o1.mm,
           STUFF((
               SELECT '|' + o2.class_key + ':' + o2.unit_key + ':' + CAST(o2.base_count AS VARCHAR(10))
               FROM erp_order o2
               WHERE o2.user_code = o1.user_code
                 AND o2.yy = o1.yy
                 AND o2.mm = o1.mm
               ORDER BY o2.class_key, o2.unit_key
               FOR XML PATH(''), TYPE
           ).value('.', 'NVARCHAR(MAX)'), 1, 1, '') AS sig
    FROM erp_order o1
    WHERE o1.yy = '2026' AND o1.mm IN ('08', '09')
    GROUP BY o1.user_code, o1.yy, o1.mm
),
LastRound AS (
    SELECT user_code, yy, mm, MAX(round) AS last_round
    FROM erp_order_history
    WHERE yy = '2026' AND mm IN ('08', '09')
    GROUP BY user_code, yy, mm
),
HistorySig AS (
    SELECT lr.user_code, lr.yy, lr.mm,
           STUFF((
               SELECT '|' + h2.class_key + ':' + h2.unit_key + ':' + CAST(h2.count AS VARCHAR(10))
               FROM erp_order_history h2
               WHERE h2.user_code = lr.user_code
                 AND h2.yy = lr.yy
                 AND h2.mm = lr.mm
                 AND h2.round = lr.last_round
               ORDER BY h2.class_key, h2.unit_key
               FOR XML PATH(''), TYPE
           ).value('.', 'NVARCHAR(MAX)'), 1, 1, '') AS sig
    FROM LastRound lr
)
SELECT COALESCE(o.user_code, h.user_code) AS user_code,
       COALESCE(o.yy, h.yy)               AS yy,
       COALESCE(o.mm, h.mm)               AS mm,
       o.sig                              AS current_order_sig,
       h.sig                              AS last_history_round_sig
FROM OrderSig o
         FULL OUTER JOIN HistorySig h
                          ON o.user_code = h.user_code
                              AND o.yy = h.yy
                              AND o.mm = h.mm
WHERE ISNULL(o.sig, '') <> ISNULL(h.sig, '');
