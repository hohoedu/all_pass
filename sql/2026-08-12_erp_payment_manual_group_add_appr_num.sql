-- 수기결제(오프라인 카드) 승인번호 저장용 컬럼
-- 결제 1건 = erp_payment_manual_group 1행 단위로 카드 승인번호가 발생하므로 그룹 테이블에 추가한다.
-- 매출자료(여신금융/홈택스) 대조 시 erp_payment_callback.appr_num 과 함께 대조 대상에 포함하기 위함.
IF COL_LENGTH('erp_payment_manual_group', 'appr_num') IS NULL
    ALTER TABLE erp_payment_manual_group ADD appr_num VARCHAR(20) NULL;
