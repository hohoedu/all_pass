-- 이북 코드 저장 테스트용 임시 테이블
-- third DB(hohoedust.hohosc_TableBookLabel) 와 구조를 동일하게 맞춘다.
-- 원본 PK 는 keycode 단독. 나중에 저장 대상을 third 로 옮길 때 컬럼 변환 없이 그대로 쓸 수 있게 하기 위함.
IF OBJECT_ID('erp_table_book_label_temp', 'U') IS NULL
    BEGIN
        CREATE TABLE erp_table_book_label_temp
        (
            keycode        VARCHAR(16)    NOT NULL,
            expiration     SMALLDATETIME  NULL,
            startdate      SMALLDATETIME  NULL,
            sample_gb      VARCHAR(50)    NULL,
            preschool_code VARCHAR(20)    NULL,
            master_code    VARCHAR(1)     NULL,
            yy             VARCHAR(4)     NULL,
            oldqty         INT            NULL,
            idate          VARCHAR(50)    NULL,
            imsi_ym        VARCHAR(7)     NULL,
            gb1            VARCHAR(2)     NULL,
            orderym        VARCHAR(6)     NULL,
            ggubun         VARCHAR(2)     NULL,
            mgubun         VARCHAR(2)     NULL,
            id             VARCHAR(50)    NULL,
            grouptag       VARCHAR(1)     NULL,
            CONSTRAINT PK_erp_table_book_label_temp PRIMARY KEY (keycode)
        );
    END

-- 지점 + 연월 조회용 (원본에는 없지만 조회 성능 때문에 추가)
IF NOT EXISTS (SELECT 1
               FROM sys.indexes
               WHERE name = 'IX_erp_table_book_label_temp_center_ym'
                 AND object_id = OBJECT_ID('erp_table_book_label_temp'))
    BEGIN
        CREATE INDEX IX_erp_table_book_label_temp_center_ym
            ON erp_table_book_label_temp (preschool_code, orderym);
    END
