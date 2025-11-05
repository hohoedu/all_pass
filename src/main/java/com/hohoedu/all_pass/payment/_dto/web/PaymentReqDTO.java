package com.hohoedu.all_pass.payment._dto.web;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class PaymentReqDTO {

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
    public static class StudentsByMonthDTO {

        private String year;
        private String month;
        private String userCode;

    }

    @Data
    public static class PaymentDTO {
        private String billId;
        private String studentName;
        private String productName;
        private String amount;
        private String hanAmount;
        private String bookAmount;
        private String statusType;
        private String eduStatus;
        private String materialStatus;
        private String message;
        private String requestDate;
        private String expiredDate;
        private String studentId;
        private String userCode;
        private String centerCode;
        private String yy;
        private String mm;
    }

    @Data
    @Builder
    public static class PaymentHistoryDTO {
        private String billId;
        private String eventType;
        private String eventSource;
        private String amount;
        private String description;
        private String paymentKey;
        private String studentId;
        private String userCode;
        private String centerCode;
    }

    @Data
    public static class ClassFeeMapDTO {
        private String fee;
        private String classKey;
    }

    @Data
    public static class BillIdSerchDTO {
        private List<Student> students;
        private String yy;
        private String mm;

        @Data
        public static class Student {
            private String studentId;
        }
    }

}
