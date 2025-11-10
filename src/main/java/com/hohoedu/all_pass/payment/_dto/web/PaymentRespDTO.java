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


}
