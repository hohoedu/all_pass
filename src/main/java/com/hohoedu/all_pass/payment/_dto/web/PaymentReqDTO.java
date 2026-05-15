package com.hohoedu.all_pass.payment._dto.web;

import lombok.*;

import java.util.List;

@Data
public class PaymentReqDTO {

    @Data
    public static class PaymentHistoryRecordDTO {
        private String eventType;
        private String eventSource;
        private String oldStatus;
        private String newStatus;
        private Integer amount;
        private String description;
        private String paymentKey;
        private String userCode;

        @Builder
        public PaymentHistoryRecordDTO(String eventType, String eventSource, String oldStatus, String newStatus, Integer amount, String description, String paymentKey, String userCode) {
            this.eventType = eventType;
            this.eventSource = eventSource;
            this.oldStatus = oldStatus;
            this.newStatus = newStatus;
            this.amount = amount;
            this.description = description;
            this.paymentKey = paymentKey;
            this.userCode = userCode;
        }
    }

    @Data
    public static class PaySendReqDTO {
        private List<String> studentIds;

        private String type;
        private String message;
        private String expireDt;
        private Integer index;
        private String yy;
        private String mm;

        private boolean includeSibling;

        private Integer customPrice;

        private String jobId;

    }

    @Data
    public static class PayDestroyReqDTO {
        private String billId;
        private String studentId;
        private String paymentKey;
        private String destroyType;
    }

    @Data
    public static class InsertBillDTO {
        private String paymentKey;
        private String billId;
        private Integer amount;
        private String status;
        private String billType;
        private String expireDate;
        private String issueDate;
        private String phone;
        private String studentId;
        private String centerCode;
        private String yy;
        private String mm;
    }

    @Data
    public static class PayCallbackDTO {
        private String apikey;
        private String bill_id;             // 청구서 ID
        private String appr_cat_id;         // ??
        private String appr_pay_type;       // 결제수단
        private String appr_card_type;      // 결제카드 종류
        private String appr_dt;             // 승인일시
        private String appr_issuer;         // 결제은행/카드명
        private String appr_issuer_cd;      // 은행 코드
        private String appr_issuer_num;     // 결제 카드 번호
        private String appr_num;            // 승인번호
        private String appr_price;          // 승인금액
        private String appr_state;          // 결제상태
        private String appr_monthly;        // 결제 할부 개월 수
        private String appr_acquirer_cd;    // 매입사 코드
        private String appr_acquirer_nm;    // 매입사 명
        private String appr_origin_dt;      // 원거래 승인일시
        private String appr_origin_num;     // 원거래 승인번호
    }

    @Data
    @Builder
    public static class ManualPaymentReqDTO {

        private List<StudentPaymentInfo> students;

        private String studentId;
        private String paymentKey;
        private String manualKey;

        private Integer cardAmount;
        private Integer cashAmount;
        private Integer transferAmount;
        private String cardName;
        private String paidDate;
        private String userCode;
        private String centerCode;
        private String yy;
        private String mm;
        private String status;
        private Integer prepaidAmount;
        private CashbillInfoDTO cashbillInfo;

        @Data
        @Builder
        public static class StudentPaymentInfo {
            private String studentId;
            private String paymentKey;
            private Integer originalAmount;
        }

        @Data
        public static class CashbillInfoDTO {
            private String studentId;       // 학생 아이디
            private String paymentKey;      // 결제한 페이먼트 키
            private String receiptNumber;   // 발급 번호
            private String issueDate;       // 발급 일자
            private String price;           // 승인 금액
            private String receiptType;     // 발급 구분 (personal: 개인, business: 사업자, self: 자진발급)
            private String supplyPrice;     // 공급가액
            private String tax;             // 세금
            private String taxType;         // 과세구분
            private String trader;          // 발급 구분 (0: 개인, 1: 사업자, 2: 자진발급)
        }
    }

    @Data
    @Builder
    public static class InsertManualGroupDTO {
        private String manualKey;
        private Integer totalAmount;
        private String cardName;
        private String method;
        private String paidDate;
        private String yy;
        private String mm;
        private String studentId;
        private String userCode;
        private String centerCode;
        private String status;
        private String cancelReason;
        private String cashbillId;
    }

    @Data
    @Builder
    public static class InsertPresetDTO {
        private String studentId;
        private String centerCode;
        private String userCode;
        private String manualKey;
        private Integer totalAmount;
        private Integer originalAmount;
        private Integer usedMonths;
        private String presetKey;
        private String status;
        private String cardName;
        private String method;
        private String paidDate;
        private Integer originalManualPaymentId;
        private String note;

    }

    @Data
    public static class PayReissueReqDTO {
        private List<String> billIds;
    }

    @Data
    public static class StudentsByMonthDTO {
        private String year;
        private String month;
        private String userCode;
        private String itemType;
    }

    @Data
    public static class PaymentCancelReqDTO {
        private String paymentKey;
        private String billId;
        private String cancelType;
        private String cancelReason;
    }


    @Data
    public static class PersonalDTO {
        private String studentId;
        private String centerCode;
        private String yy;
        private String mm;
    }

    @Data
    public static class EduFeeUpdateReqDTO {
        private String studentId;
        private String paymentKey;
        private String yy;
        private String mm;
        private Integer hanEduFee;
        private Integer hanMaterialFee;
        private Integer bookEduFee;
        private Integer bookMaterialFee;
    }

    @Data
    public static class CashbillIssueReqDTO {
        private String studentId;       // 학생 아이디
        private String paymentKey;      // 결제한 페이먼트 키
        private String receiptNumber;   // 발급 번호
        private String issueDate;       // 발급 일자
        private String price;           // 승인 금액
        private String receiptType;     // 발급 구분 (personal: 개인, business: 사업자, self: 자진발급)
        private String supplyPrice;     // 공급가액
        private String tax;             // 세금
        private String taxType;         // 과세구분
        private String trader;          // 발급 구분 (0: 개인, 1: 사업자)
    }

    @Data
    public static class CashbillStudentReqDTO {
        private String year;
        private String month;
    }

    @Data
    public static class CashbillCancelDTO {
        String billId;
        String reason;
    }

    @Data
    public static class ClaimFilterDTO {
        private String userCode;
        private String itemType;
        private String centerCode;
        private String yy;
        private String mm;
    }

    @Data
    public static class UnpaidStudentReqDTO {
        private String year;
        private String month;
        private String centerCode;
    }

    @Data
    public static class UpdatePaymentDTO {
        private String paidDate;        // "2026-03-15"
        private Long cardAmount;
        private Long cashAmount;
        private Long transferAmount;
        private String cardName;        // 카드사 코드
    }

    @Data
    public static class UpdateBillingPhone{
        private String studentId;
        private String phone;
    }


    @Data
    public static class YearMonthDTO{
        private String year;
        private String month;
    }
}
