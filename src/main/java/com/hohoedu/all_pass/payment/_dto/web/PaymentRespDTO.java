package com.hohoedu.all_pass.payment._dto.web;

import lombok.Data;

@Data
public class PaymentRespDTO {

    @Data
    public static class AssignStudentsDTO {
        private String studentId;
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
        private String totalPrice;
        private String totalFee;
        private String totalMaterialFee;
        private String parentPhone;

    }

    @Data
    public static class ClassFeeMapDTO {
        private String classKey;
        private String className;
        private String fee;
        private String classType;
    }

    @Data
    public static class PaymentDetailDTO {
        private String studentId;
        private String studentName; // 학생 이름
        private String classDate; // 수강 년월
        private String subject; // 수강과목
        private String hanTeacher; // 한자 담임
        private String bookTeacher; // 독서 담임
        private String billId; // 청구서 아이디
        private String billType; // 청구 종류
        private String approvedDate; // 결제 일
        private String amount; // 결제 금액
        private String status;
    }

    @Data
    public static class PaymentBillIdDTO{
        private String studentId;
        private String studentName;
        private String billId;
        private String amount;
        private String status;
    }
}
