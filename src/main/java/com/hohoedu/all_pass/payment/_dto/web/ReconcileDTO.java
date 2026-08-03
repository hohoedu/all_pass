package com.hohoedu.all_pass.payment._dto.web;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 외부 매출자료(여신금융협회 / 홈택스) ↔ erp_payment_callback 승인번호 대조 DTO
 */
public class ReconcileDTO {

    /** 엑셀에서 파싱한 한 건(매출) */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExcelRow {
        private String apprNum;   // 승인번호(정규화 전 원본)
        private String txDate;    // 거래일자
        private String cardName;  // 카드사
        private String amount;    // 승인금액
        private String gubun;     // 구분(승인/취소)
    }

    /** DB(erp_payment_callback) 대조 대상 한 건 */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CallbackRow {
        private String apprNum;   // 승인번호
        private String apprDate;  // 승인일시
        private String apprPrice; // 승인금액
        private String apprIssuer;// 카드/은행명
        private String apprState; // 결제상태
    }

    /** 대조 결과 응답 */
    @Data
    @Builder
    public static class Result {
        private String source;        // yeoshin / hometax
        private int fileTotal;        // 엑셀 전체 건수
        private int fileApproved;     // 엑셀 승인 건수(대조 대상)
        private int dbTotal;          // DB 대조 대상 건수
        private int matched;          // 일치 건수
        private String dateFrom;      // 대조 거래일자 시작
        private String dateTo;        // 대조 거래일자 끝

        /** 🔴 파일엔 있으나 DB에 없음 → 매출 확인 필요 */
        private List<ExcelRow> missingInDb;
        /** 🟡 DB엔 있으나 파일에 없음 → 반영지연/취소 의심 */
        private List<CallbackRow> missingInFile;
    }

    /** 엑셀 다운로드용 요청(대조 결과를 그대로 되돌려 받아 파일 생성) */
    @Data
    public static class ExportRequest {
        private Result result;
    }
}
