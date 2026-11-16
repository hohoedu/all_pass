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
        private String amount;    // 승인금액 (취소는 음수로 들어옴)
        private String gubun;     // 구분(승인/취소)
    }

    /** DB 대조 대상 한 건 (결제선생 콜백 / 수기결제 그룹) */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CallbackRow {
        private String apprNum;    // 승인번호
        private String apprDate;   // 승인일시
        private String apprPrice;  // 승인금액
        private String apprIssuer; // 카드/은행명
        private String apprState;  // 결제상태
        private String centerCode; // 센터 (콜백 저장 시 bill 미해결이면 NULL)
        private boolean canceled;  // 취소 여부 (청구서 canceled 또는 취소 상태)
    }

    /** 승인번호는 맞지만 금액·취소 상태가 다른 건 */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MismatchRow {
        private String apprNum;
        private String txDate;
        private String cardName;
        private long fileAmount; // 엑셀 순금액 (승인 - 취소)
        private long dbAmount;   // DB 유효 결제 금액 (취소 제외)
        private String note;
    }

    /** 대조 결과 응답 */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Result {
        private String source;        // yeoshin / hometax
        private int fileTotal;        // 엑셀 전체 건수
        private int fileApproved;     // 엑셀 승인 건수
        private int fileCanceled;     // 엑셀 취소 건수
        private int dbTotal;          // DB 대조 대상 건수
        private int matched;          // 일치 건수(승인번호 + 금액)
        private String dateFrom;      // 대조 거래일자 시작
        private String dateTo;        // 대조 거래일자 끝

        // 금액 합계
        private long fileApprovedAmount; // 엑셀 승인 합계
        private long fileCanceledAmount; // 엑셀 취소 합계 (양수)
        private long fileNetAmount;      // 엑셀 순매출 = 승인 - 취소
        private long dbAmount;           // DB 카드 결제 합계 (취소 제외)
        private long diffAmount;         // 엑셀 순매출 - DB
        private long prevCancelAmount;   // 이전 달 승인분 취소 합계 (양수, 차이 설명용)
        private long unknownCenterAmount;// 센터 미확인 DB 결제 합계 (대조 제외)

        /** 🔴 파일엔 있으나 DB에 없음 → 매출 확인 필요 */
        private List<ExcelRow> missingInDb;
        /** 🟡 DB엔 있으나 파일에 없음 → 반영지연/취소 의심 */
        private List<CallbackRow> missingInFile;
        /** 🟠 엑셀에서 취소됐는데 DB 결제는 살아 있음 → 취소 미반영 */
        private List<MismatchRow> cancelNotReflected;
        /** 🟠 승인번호는 같은데 금액이 다름 (부분취소, DB만 취소 포함) */
        private List<MismatchRow> amountMismatch;
        /** ℹ️ 이전 달 승인을 이번 달에 취소한 건 */
        private List<ExcelRow> prevMonthCancels;
        /** ℹ️ 센터 미확인(center_code NULL) DB 결제 중 엑셀과 매칭 안 된 건 → 다른 센터 결제일 수 있어 제외 */
        private List<CallbackRow> unknownCenter;
    }

    /** 엑셀 다운로드용 요청(대조 결과를 그대로 되돌려 받아 파일 생성) */
    @Data
    public static class ExportRequest {
        private Result result;
    }
}
