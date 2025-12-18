package com.hohoedu.all_pass.payment._dto.web;

import lombok.Data;

@Data
public class PaymentRespDTO {

    @Data
    public static class AssignStudentsDTO {
        private String studentId;
        private String paymentKey;
        private String billId;
        private String studentName;
        private String subject;
        private String hanTeacher;
        private String bookTeacher;
        private Integer hanFee;
        private Integer hanMaterialFee;
        private Integer bookFee;
        private Integer bookMaterialFee;
        private String eduStatus;
        private String materialStatus;
        private String totalStatus;
        private String totalPrice;
        private String unpaidAmount;
        private String amountDue;
        private String totalFee;
        private String totalMaterialFee;
        private String parentPhone;
    }

    @Data
    public static class PaymentConfigDTO {
        private String centerCode;
        private String apiKey;
        private String memberId;
        private String merchantId;
        private String preBillId;
        private String callbackUrl;
        private String sendUrl;
        private String destroyUrl;
        private String cancelUrl;
    }

    @Data
    public static class PaySendRespDTO {
        private String billId;
        private String paymintCode;     // Paymint 응답코드
        private String paymintMsg;      // Paymint 메시지
        private boolean dbSaved;
    }

    @Data
    public static class PaymintRespDTO {
        private String code;
        private String msg;
    }

    @Data
    public static class ClassFeeMapDTO {
        private String classKey;
        private String className;
        private String fee;
        private String classType;
        private String unitKey;
        private String unitName;
    }

    @Data
    public static class PaymentModalDTO {
        private String studentId;
        private String studentName;
        private String paymentKey;
        private String classDate;
        private String subject;
        private String hanTeacher;
        private String bookTeacher;
        private String billType;
        private Integer amount;
        private String status;
        private String paidDate;
        private Integer hanMaterialFee;
        private Integer bookMaterialFee;
        private Integer hanFee;
        private Integer bookFee;
    }

    @Data
    public static class UnpaidStudentDTO {
        private String studentId;
        private String studentName;
        private String gradeName;
        private String hanTeacher;
        private String bookTeacher;
    }

    @Data
    public static class ManualPaymentRespDTO {
        private String paymentKey;
        private String studentId;
        private String billId;
        private Integer price;
        private String message;
    }

}
