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
        private List<String> studentIds;

        private String type;
        private String message;
        private String expireDt;
        private Integer index;
        private String yy;
        private String mm;

        private boolean includeSibling;

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
    public static class MonthlyPaymentReqDTO {
        private String centerCode;
        private String yy;
        private String mm;
    }

    @Data
    public static class ManualPaymentReqDTO {

        private String studentId;
        private String paymentKey;
        private Integer cardAmount;
        private Integer cashAmount;
        private Integer transferAmount;
        private String cardName;
        private String paidDate;
        private String userCode;
        private String yy;
        private String mm;

    }

    @Data
    public static class StudentsByMonthDTO {
        private String year;
        private String month;
        private String userCode;
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
        private String yy;
        private String mm;
        private Integer hanEduFee;
        private Integer hanMaterialFee;
        private Integer bookEduFee;
        private Integer bookMaterialFee;
    }
}
