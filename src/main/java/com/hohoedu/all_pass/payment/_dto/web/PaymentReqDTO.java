package com.hohoedu.all_pass.payment._dto.web;

import lombok.Builder;
import lombok.Data;

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
        private String studentId;
        private String studentName;
        private String phone;
        private Integer price;
        private String type;
        private String message;
        private String expireDt;
        private Integer index;
        private String yy;
        private String mm;
    }

    @Data
    public static class PayDestroyReqDTO {
        private String studentId;
        private String paymentKey;
        private String billId;
        private Integer price;
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
        private String studentId;
        private String centerCode;
        private String yy;
        private String mm;
    }

    @Data
    public static class PayCallbackDTO {
        private String apikey;
        private String bill_id;
        private String appr_pay_type;
        private String appr_card_type;
        private String appr_dt;
        private String appr_issuer;
        private String appr_num;
        private String appr_price;
        private String appr_state;
    }

    @Data
    public static class ManualPaymentReqDTO {

        private String studentId;
        private String centerCode;
        private String year;
        private String month;

        private boolean eduFee;   // 교육비 체크 여부
        private boolean bookFee;  // 교재비 체크 여부

        private Integer eduCard;
        private Integer eduCash;
        private Integer eduTransfer;

        private Integer bookCard;
        private Integer bookCash;
        private Integer bookTransfer;

        private Integer prepayMonth;     // 선결제 개월 수
        private String prepayStartYm;    // 선결제 시작 เดือน
        private String note;             // 비고

    }

    @Data
    public static class StudentsByMonthDTO {
        private String year;
        private String month;
        private String userCode;
    }

    @Data
    public static class PaymentCancelReqDTO {
        private String studentId;
        private String paymentKey;
        private String billId;
        private String yy;
        private String mm;
        private boolean cancelEdu;
        private boolean cancelBook;
        private String cancelReason;
    }


    @Data
    public static class PersonalDTO {
        private String studentId;
        private String centerCode;
        private String yy;
        private String mm;
    }
}
