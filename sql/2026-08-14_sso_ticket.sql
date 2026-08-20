-- 올패스 ↔ 호호책방 SSO 토큰 브릿지용 공유 테이블 (2026-08-14)
--
-- dbhohoedu_stst DB를 all_pass/book_clinic이 함께 쓰므로 실제 CREATE는 한 번만 실행하면 된다.
-- book_clinic 쪽에도 동일 DDL이 src/main/resources/db/ddl-sso.sql로 문서화되어 있다(멱등 가드라
-- 두 번 실행해도 안전). 용도: 직원이 자기 계정 그대로 서로 다른 도메인의 상대 앱으로 이동할 때
-- 발급되는 원타임 서명 토큰(JWT)의 jti를 저장해 재사용(replay)을 막는다.
IF OBJECT_ID('sso_ticket', 'U') IS NULL
BEGIN
    CREATE TABLE sso_ticket (
        jti          VARCHAR(36)   NOT NULL PRIMARY KEY,
        issuer       VARCHAR(20)   NOT NULL,                    -- 'all-pass' | 'book-clinic'
        user_id      VARCHAR(100)  NOT NULL,                    -- erp_user.user_id
        redirect_url VARCHAR(500)  NULL,
        used_yn      BIT           NOT NULL DEFAULT 0,
        issued_at    DATETIME2     NOT NULL DEFAULT DATEADD(HOUR, 9, GETUTCDATE()),
        expires_at   DATETIME2     NOT NULL
    );
END

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'ix_sso_ticket_expires_at' AND object_id = OBJECT_ID('sso_ticket'))
BEGIN
    CREATE INDEX ix_sso_ticket_expires_at ON sso_ticket (expires_at);
END
